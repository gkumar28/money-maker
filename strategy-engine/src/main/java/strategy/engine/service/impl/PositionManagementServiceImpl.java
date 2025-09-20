package strategy.engine.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import strategy.engine.constant.enums.TradeDirection;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.Order;
import strategy.engine.service.PortfolioService;
import strategy.engine.service.PositionManagementService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static strategy.engine.util.StrategyEngineUtils.sanitize;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionManagementServiceImpl implements PositionManagementService {

    private static final double MAX_CAPITAL_ALLOCATION_PCT = 0.02;  // 2% risk per trade
    private static final int MIN_LOT_SIZE = 1;

    // Entry position constants
    private static final double SL_MULTIPLIER = 0.05;
    private static final double TP_MULTIPLIER = 0.125;

    // Exit position constants
    private static final double MIN_CONFIDENCE_TO_SELL = 0.5;
    private static final double MIN_CONFIDENCE_TO_CLOSE = 0.8;
    private static final double MINIMUM_EXIT_POSITION_PCT = 0.2;

    // capital allocation constants
    private static final double CAPITAL_ALLOCATION_WEAK_TREND_MULTIPLIER = 0.5;
    private static final double CAPITAL_ALLOCATION_STRONG_TREND_MULTIPLIER = 1.2;

    private final PortfolioService portfolioService;
    private final Map<String, BigDecimal> slPrices;
    private final Map<String, BigDecimal> tpPrices;


    @Override
    public Order createOrderForLongPosition(String instrument, Signal signal) {
        if (null == signal.getDirection()) {
            return Order.empty(instrument, signal);
        }

        return switch (signal.getDirection()) {
            case BUY -> calculateLongPositionEntrySize(instrument, signal);
            case SELL -> calculateLongPositionExitSize(instrument, signal);
        };
    }

    @Override
    public Order calculateLongPositionExitSize(String instrument, Signal signal) {
        Holding holdings = portfolioService.getCurrentHoldings(instrument);
        if (null == holdings || holdings.getQuantity() == 0) {
            log.debug("{}: No Exit order as no current holdings", instrument);
            return Order.empty(instrument, signal);
        }
        int currentHoldings = holdings.getQuantity();
        BigDecimal confidence = signal.getConfidence(); // C
        BigDecimal minExitPositionPct = BigDecimal.valueOf(MINIMUM_EXIT_POSITION_PCT); // A
        BigDecimal minConfidenceToClose = BigDecimal.valueOf(MIN_CONFIDENCE_TO_CLOSE); // C100
        BigDecimal minConfidenceToSell = BigDecimal.valueOf(MIN_CONFIDENCE_TO_SELL); // C0

        // confidence span CS = C100 - C0
        BigDecimal confidenceSpan = minConfidenceToClose.subtract(minConfidenceToSell);
        // normalized confidence CN = min(1.0, (C - C0)/(C100 - C0))
        BigDecimal normalizedConfidence = BigDecimal.ONE.min(
            confidence.subtract(minConfidenceToSell)
                .divide(confidenceSpan, 4, RoundingMode.HALF_UP)
        );
        // scale = A + min(1.0, (C - C0)/(C100 - C0)) * (1 - A)
        BigDecimal scale = minExitPositionPct.add(normalizedConfidence.multiply(BigDecimal.ONE.subtract(minExitPositionPct)));

        int quantity = (int) Math.floor(currentHoldings * scale.doubleValue());
        if (quantity < MIN_LOT_SIZE) {
            quantity = 0;  // Don't place tiny sell orders
        }

        log.debug("{}: Exit order price: quantity: {} price: {}", instrument, quantity, sanitize(signal.getPrice()));
        return new Order(
            instrument,
            signal.getTimestamp(),
            TradeDirection.SELL,
            quantity,
            signal.getPrice(),
            signal.getConfidence(),
            signal.getPrice().multiply(BigDecimal.valueOf(quantity))  // capital released
        );
    }

    @Override
    public Order calculateLongPositionEntrySize(String instrument, Signal signal) {
        // Base capital allocation (2% of account)
        BigDecimal baseCapital = portfolioService.getTotalValue().multiply(BigDecimal.valueOf(MAX_CAPITAL_ALLOCATION_PCT));
        BigDecimal finalCapitalAllocation = getFinalCapitalAllocation(signal, baseCapital);

        // 7. Calculate quantity = capital allocation / entry price
        int quantity = finalCapitalAllocation.divide(signal.getPrice(), 4, RoundingMode.HALF_UP).intValue();
        if (quantity < MIN_LOT_SIZE) {
            quantity = 0; // Could choose to skip trade or round up to min lot size
        }

        log.debug("{}: Entry order price: quantity: {} price: {}", instrument, quantity, sanitize(signal.getPrice()));
        return new Order(
            instrument,
            signal.getTimestamp(),
            TradeDirection.BUY,
            quantity,
            signal.getPrice(),
            signal.getConfidence(),
            finalCapitalAllocation
        );
    }

    @Override
    public Order triggerSLTPForPosition(String instrument, Signal signal, BigDecimal currentPrice) {
        Holding currentHolding = portfolioService.getPortfolio().getHoldings().getOrDefault(instrument, new Holding(instrument));
        BigDecimal slPrice = slPrices.get(instrument);
        BigDecimal tpPrice = tpPrices.get(instrument);
        if (0 == currentHolding.getQuantity() || slPrice == null || tpPrice == null) {
            return null;
        }

        if (slPrice.compareTo(currentPrice) < 0 && tpPrice.compareTo(currentPrice) > 0) {
           return null;
        }

        if (slPrice.compareTo(currentPrice) >= 0) {
            log.debug("{}: SL triggered SL: {} current: {}", instrument, sanitize(slPrice), sanitize(currentPrice));
        }

        if (tpPrice.compareTo(currentPrice) <= 0) {
            log.debug("{}: TP triggered TP: {} current: {}", instrument, sanitize(tpPrice), sanitize(currentPrice));
        }


        slPrices.remove(instrument);
        tpPrices.remove(instrument);
        return new Order(instrument,
            signal.getTimestamp(),
            TradeDirection.SELL,
            currentHolding.getQuantity(),
            currentPrice,
            BigDecimal.ONE,
            null);
    }

    private BigDecimal getFinalCapitalAllocation(Signal signal, BigDecimal baseCapital) {

        BigDecimal trendMultiplier = BigDecimal.ONE;
        if (signal.getAdx().doubleValue() < 20) {
            trendMultiplier = BigDecimal.valueOf(CAPITAL_ALLOCATION_WEAK_TREND_MULTIPLIER);      // Weak trend → reduce size
        } else if (signal.getAdx().doubleValue() > 30) {
            trendMultiplier = BigDecimal.valueOf(CAPITAL_ALLOCATION_STRONG_TREND_MULTIPLIER);     // Strong trend → increase size
        }

        // Calculate final capital to allocate for this trade
        return baseCapital.multiply(trendMultiplier).multiply(signal.getConfidence());
    }

    @Override
    public void updateSlTpForInstrument(String instrument) {
        if (updateStopLoss(instrument) && updateTakeProfit(instrument)) {
            log.debug("{}: updating SL-TP, SL: {} TP: {}", instrument, sanitize(slPrices.get(instrument)), sanitize(tpPrices.get(instrument)));
        }
    }

    private boolean updateStopLoss(String instrument) {
        Holding holding = portfolioService.getCurrentHoldings(instrument);
        if (holding.getQuantity() == 0) {
            return false;
        }

        BigDecimal avgEntryPrice = holding.getCurrentInvestedCapital().divide(BigDecimal.valueOf(holding.getQuantity()), RoundingMode.HALF_UP);

        BigDecimal slDistance = avgEntryPrice.multiply(BigDecimal.valueOf(SL_MULTIPLIER));
        BigDecimal stopLoss = avgEntryPrice.subtract(slDistance);
        if (stopLoss.compareTo(BigDecimal.ZERO) < 0) {
            stopLoss = BigDecimal.ZERO;
        }
        slPrices.put(instrument, stopLoss);
        return true;
    }

    private boolean updateTakeProfit(String instrument) {
        Holding holding = portfolioService.getCurrentHoldings(instrument);
        if (holding.getQuantity() == 0) {
            return false;
        }

        BigDecimal avgEntryPrice = holding.getCurrentInvestedCapital().divide(BigDecimal.valueOf(holding.getQuantity()), RoundingMode.HALF_UP);

        BigDecimal tpDistance = avgEntryPrice.multiply(BigDecimal.valueOf(TP_MULTIPLIER));
        BigDecimal takeProfit = avgEntryPrice.add(tpDistance);
        tpPrices.put(instrument, takeProfit);
        return true;
    }
}

