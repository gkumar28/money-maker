package strategy.engine.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Trade;
import org.ta4j.core.num.DecimalNum;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Portfolio;
import strategy.engine.schemaobject.signal.Signal;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.signal.SignalContext;
import strategy.engine.service.PortfolioManagementService;
import strategy.engine.service.RiskManagementService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import static strategy.engine.util.StrategyEngineUtils.sanitize;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskManagementServiceImpl implements RiskManagementService {

    private static final BigDecimal minExposureChange = BigDecimal.valueOf(0.2);
    private static final BigDecimal MAX_EXPOSURE = BigDecimal.valueOf(0.2); // 20% max exposure per instrument
    private static final BigDecimal BASE_CAPITAL_ALLOCATION_PCT = BigDecimal.valueOf(0.05);  // 5% base capital per trade including cost
    private static final BigDecimal MAX_CAPITAL_ALLOCATION_PCT = BigDecimal.valueOf(0.10);  // 10% max capital per trade including cost
    private static final BigDecimal MIN_LOT_SIZE = BigDecimal.valueOf(1);

    // SL-TP multipliers
    private static final BigDecimal SL_MULTIPLIER = BigDecimal.valueOf(0.05);
    private static final BigDecimal TP_MULTIPLIER = BigDecimal.valueOf(0.10);

    // capital allocation constants
    private static final BigDecimal CAPITAL_ALLOCATION_WEAK_TREND_MULTIPLIER = BigDecimal.valueOf(0.5);
    private static final BigDecimal CAPITAL_ALLOCATION_STRONG_TREND_MULTIPLIER = BigDecimal.valueOf(1.2);

    private static final boolean WHOLE_QUANTITY_ONLY = true;

    private final PortfolioManagementService portfolioManagementService;


    @Override
    public SignalContext createOrder(Portfolio portfolio, String instrument, SignalContext signalContext) {
        if (signalContext.signal().shouldBeDiscarded()) {
            return SignalContext.instance();
        }

        BigDecimal portfolioValue = portfolio.snapshot().value();
        BigDecimal instrumentValue = portfolio.getHolding(instrument).value();
        BigDecimal signal = signalContext.signal().exposure();
        BigDecimal currentExposure = instrumentValue.divide(portfolioValue, RoundingMode.HALF_UP);
        BigDecimal targetExposure = signal.multiply(MAX_EXPOSURE);

        // case 1: entry
        if (instrumentValue.equals(BigDecimal.ZERO)) {
            return calculateLongEntrySize(portfolio, instrument, currentExposure, targetExposure, signalContext);
        }

        // case 2: exit
        if (targetExposure.equals(BigDecimal.ZERO)) {
            return calculateLongExitSize(portfolio, instrument, currentExposure, targetExposure, signalContext);
        }
        // case 3: expand/trim
        if (currentExposure.compareTo(targetExposure) < 0) {
            return calculateLongEntrySize(portfolio, instrument, currentExposure, targetExposure, signalContext);
        } else {
            return calculateLongExitSize(portfolio, instrument, currentExposure, targetExposure, signalContext);
        }
    }

    private SignalContext calculateLongEntrySize(Portfolio portfolio,
                                         String instrument,
                                         BigDecimal currentExposure,
                                         BigDecimal targetExposure,
                                         SignalContext signalContext) {
        Portfolio.Snapshot snapshot = portfolio.snapshot();
        // calculate estimated quantity for the trade using fixed capital allocation
        // using base capital allocation (2% of account)
        BigDecimal baseCapital = snapshot.availableCapital().multiply(BASE_CAPITAL_ALLOCATION_PCT);
        BigDecimal allocatedCapital = getEstimatedCapitalAllocation(baseCapital, signalContext);

        BigDecimal capitalToReachTarget = snapshot.value().multiply(targetExposure.subtract(currentExposure));

        BigDecimal maxAllowedAllocation = snapshot.availableCapital().multiply(MAX_CAPITAL_ALLOCATION_PCT);

        BigDecimal capitalToBeInvested = allocatedCapital.min(capitalToReachTarget).min(maxAllowedAllocation)
                .subtract(getEstimatedCost(Trade.TradeType.BUY));
        BigDecimal actualTargetExposure = portfolio.getHolding(instrument).value().add(capitalToBeInvested)
                .divide(snapshot.value(), RoundingMode.HALF_UP);

        if (actualTargetExposure.subtract(currentExposure).abs().compareTo(minExposureChange) <= 0) {
            return SignalContext.instance();
        }

        if (log.isDebugEnabled()) {
            log.debug("{}: Entry order - price: {} capital allocated: {} current exposure: {} target exposure: {}",
                    instrument,
                    sanitize(signalContext.metaData().price()),
                    sanitize(capitalToBeInvested),
                    sanitize(currentExposure),
                    sanitize(actualTargetExposure)
            );
        }

        return signalContext.withSignal(signalContext.signal().withExposure(actualTargetExposure));
    }

    private BigDecimal getEstimatedCapitalAllocation(BigDecimal capital, SignalContext signalContext) {
        BigDecimal trendMultiplier = BigDecimal.ONE;
        if (signalContext.metaData().adx().doubleValue() < 20) {
            trendMultiplier = CAPITAL_ALLOCATION_WEAK_TREND_MULTIPLIER;      // Weak trend → reduce size
        } else if (signalContext.metaData().adx().doubleValue() > 30) {
            trendMultiplier = CAPITAL_ALLOCATION_STRONG_TREND_MULTIPLIER;     // Strong trend → increase size
        }
        return capital.multiply(trendMultiplier);
    }

    private BigDecimal getEstimatedCost(Trade.TradeType tradeType) {
        // to read policy here and get estimate
        return BigDecimal.ZERO;
    }

    private SignalContext calculateLongExitSize(Portfolio portfolio,
                                        String instrument,
                                        BigDecimal currentExposure,
                                        BigDecimal targetExposure,
                                        SignalContext signalContext) {
        Holding holding = portfolio.getHolding(instrument);
        if (null == holding || BigDecimal.ZERO.compareTo(holding.quantity()) == 0) {
            log.debug("{}: No Exit order as no current holdings", instrument);
            return SignalContext.instance();
        }

        if (targetExposure.subtract(currentExposure).abs().compareTo(minExposureChange) <= 0) {
            return SignalContext.instance();
        }

        if (log.isDebugEnabled()) {
            log.debug("{}: Exit order - current exposure: {} target exposure: {} price: {}",
                    instrument,
                    sanitize(currentExposure),
                    sanitize(targetExposure),
                    sanitize(signalContext.metaData().price()));
        }

        return signalContext;
    }

    @Override
    public SignalContext triggerSLTP(Portfolio portfolio, String instrument, SignalContext signalContext) {
        Holding currentHolding = portfolio.getHolding(instrument);
        BigDecimal slPrice = portfolio.getSlPrice(instrument);
        BigDecimal tpPrice = portfolio.getTpPrice(instrument);
        if (BigDecimal.ZERO.compareTo(currentHolding.quantity()) == 0 || slPrice == null || tpPrice == null) {
            return SignalContext.instance();
        }

        BigDecimal currentPrice = signalContext.metaData().price();
        if (slPrice.compareTo(currentPrice) < 0 && currentPrice.compareTo(tpPrice) < 0) {
           return SignalContext.instance();
        }

        if (slPrice.compareTo(currentPrice) >= 0) {
            log.debug("{}: SL triggered SL: {} current: {}", instrument, sanitize(slPrice), sanitize(currentPrice));
        }

        if (tpPrice.compareTo(currentPrice) <= 0) {
            log.debug("{}: TP triggered TP: {} current: {}", instrument, sanitize(tpPrice), sanitize(currentPrice));
        }


        portfolio.removeSlPrice(instrument);
        portfolio.removeTpPrice(instrument);
        return signalContext.withSignal(signalContext.signal().withExposure(BigDecimal.valueOf(0.0)));
    }

    @Override
    public void updateSLTP(Portfolio portfolio, String instrument) {
        Holding holding = portfolio.getHolding(instrument);
        if (BigDecimal.ZERO.compareTo(holding.quantity()) == 0) {
            return;
        }

        BigDecimal avgEntryPrice = holding.investedCapital().divide(holding.quantity(), RoundingMode.HALF_UP);

        BigDecimal slDistance = avgEntryPrice.multiply(SL_MULTIPLIER);
        BigDecimal stopLoss = avgEntryPrice.subtract(slDistance);
        if (stopLoss.compareTo(BigDecimal.ZERO) < 0) {
            stopLoss = BigDecimal.ZERO;
        }
        portfolio.putSlPrice(instrument, stopLoss);

        BigDecimal tpDistance = avgEntryPrice.multiply(TP_MULTIPLIER);
        BigDecimal takeProfit = avgEntryPrice.add(tpDistance);
        portfolio.putTpPrice(instrument, takeProfit);
        if (log.isDebugEnabled()) {
            log.debug("{}: updating SL-TP, SL: {} TP: {}", instrument, sanitize(portfolio.getSlPrices().get(instrument)), sanitize(portfolio.getTpPrices().get(instrument)));
        }
    }
}

