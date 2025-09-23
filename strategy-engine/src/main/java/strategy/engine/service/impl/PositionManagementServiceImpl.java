package strategy.engine.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import strategy.engine.constant.enums.TradeAction;
import strategy.engine.constant.enums.TradeType;
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

    // SL-TP multipliers
    private static final double SL_MULTIPLIER = 0.05;
    private static final double TP_MULTIPLIER = 0.10;

    // capital allocation constants
    private static final double CAPITAL_ALLOCATION_WEAK_TREND_MULTIPLIER = 0.5;
    private static final double CAPITAL_ALLOCATION_STRONG_TREND_MULTIPLIER = 1.2;

    private static final boolean WHOLE_QUANTITY_ONLY = true;

    private final PortfolioService portfolioService;
    private final Map<String, BigDecimal> slPrices;
    private final Map<String, BigDecimal> tpPrices;


    @Override
    public Order createOrderForLongPosition(String instrument, Signal signal) {
        if (null == signal.getTradeType()) {
            return Order.empty(instrument, signal);
        }

        return switch (signal.getAction()) {
            case ENTRY,EXPAND -> calculateLongPositionEntrySize(instrument, signal);
            case TRIM,EXIT -> calculateLongPositionExitSize(instrument, signal);
        };
    }

    @Override
    public Order calculateLongPositionExitSize(String instrument, Signal signal) {
        Holding holdings = portfolioService.getCurrentHoldings(instrument);
        if (null == holdings || BigDecimal.ZERO.compareTo(holdings.getQuantity()) == 0) {
            log.debug("{}: No Exit order as no current holdings", instrument);
            return Order.empty(instrument, signal);
        }
        BigDecimal currentHoldings = holdings.getQuantity();
        BigDecimal scale = BigDecimal.ONE; // EXIT assumed
        if (TradeAction.TRIM == signal.getAction()) {
            scale = BigDecimal.valueOf(0.2);
        }
        BigDecimal quantity = currentHoldings.multiply(scale);
        quantity = getFinalQuantityBasedOnPolicy(quantity);

        log.debug("{}: Exit order price: quantity: {} price: {}", instrument, sanitize(quantity), sanitize(signal.getPrice()));
        return new Order(
            instrument,
            signal.getTimestamp(),
            TradeType.SELL,
            quantity,
            signal.getPrice(),
            getEstimatedCost(signal.getTradeType()) // capital released
        );
    }

    @Override
    public Order calculateLongPositionEntrySize(String instrument, Signal signal) {
        // Base capital allocation (2% of account)
        BigDecimal baseCapital = portfolioService.getTotalValue().multiply(BigDecimal.valueOf(MAX_CAPITAL_ALLOCATION_PCT));
        BigDecimal finalCapitalAllocation = getFinalCapitalAllocation(signal, baseCapital);
        if (TradeAction.TRIM == signal.getAction()) {
            finalCapitalAllocation = finalCapitalAllocation.multiply(BigDecimal.valueOf(0.2));
        }
        // 7. Calculate quantity = capital allocation / entry price
        BigDecimal quantity = finalCapitalAllocation.divide(signal.getPrice(), 4, RoundingMode.HALF_UP);
        quantity = getFinalQuantityBasedOnPolicy(quantity);

        log.debug("{}: Entry order price: quantity: {} price: {}", instrument, sanitize(quantity), sanitize(signal.getPrice()));
        return new Order(
            instrument,
            signal.getTimestamp(),
            TradeType.BUY,
            quantity,
            signal.getPrice(),
            finalCapitalAllocation
        );
    }

    @Override
    public Order triggerSLTPForPosition(String instrument, Signal signal, BigDecimal currentPrice) {
        Holding currentHolding = portfolioService.getPortfolio().getHoldings().getOrDefault(instrument, new Holding(instrument));
        BigDecimal slPrice = slPrices.get(instrument);
        BigDecimal tpPrice = tpPrices.get(instrument);
        if (BigDecimal.ZERO.compareTo(currentHolding.getQuantity()) == 0 || slPrice == null || tpPrice == null) {
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
        return new Order(
            instrument,
            signal.getTimestamp(),
            TradeType.SELL,
            currentHolding.getQuantity(),
            currentPrice,
            getEstimatedCost(signal.getTradeType()));
    }

    private BigDecimal getFinalCapitalAllocation(Signal signal, BigDecimal baseCapital) {

        BigDecimal trendMultiplier = BigDecimal.ONE;
        if (signal.getAdx().doubleValue() < 20) {
            trendMultiplier = BigDecimal.valueOf(CAPITAL_ALLOCATION_WEAK_TREND_MULTIPLIER);      // Weak trend → reduce size
        } else if (signal.getAdx().doubleValue() > 30) {
            trendMultiplier = BigDecimal.valueOf(CAPITAL_ALLOCATION_STRONG_TREND_MULTIPLIER);     // Strong trend → increase size
        }

        // Calculate final capital to allocate for this trade
        return baseCapital.multiply(trendMultiplier).add(getEstimatedCost(signal.getTradeType()));
    }

    private BigDecimal getEstimatedCost(TradeType tradeType) {
        // to read policy here and get estimate
        return BigDecimal.ZERO;
    }

    @Override
    public void updateSlTpForInstrument(String instrument) {
        if (updateStopLoss(instrument) && updateTakeProfit(instrument)) {
            log.debug("{}: updating SL-TP, SL: {} TP: {}", instrument, sanitize(slPrices.get(instrument)), sanitize(tpPrices.get(instrument)));
        }
    }

    private boolean updateStopLoss(String instrument) {
        Holding holding = portfolioService.getCurrentHoldings(instrument);
        if (BigDecimal.ZERO.compareTo(holding.getQuantity()) == 0) {
            return false;
        }

        BigDecimal avgEntryPrice = holding.getCurrentInvestedCapital().divide(holding.getQuantity(), RoundingMode.HALF_UP);

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
        if (BigDecimal.ZERO.compareTo(holding.getQuantity()) == 0) {
            return false;
        }

        BigDecimal avgEntryPrice = holding.getCurrentInvestedCapital().divide(holding.getQuantity(), RoundingMode.HALF_UP);

        BigDecimal tpDistance = avgEntryPrice.multiply(BigDecimal.valueOf(TP_MULTIPLIER));
        BigDecimal takeProfit = avgEntryPrice.add(tpDistance);
        tpPrices.put(instrument, takeProfit);
        return true;
    }

    private BigDecimal getFinalQuantityBasedOnPolicy(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.valueOf(MIN_LOT_SIZE)) <= 0) {
            return BigDecimal.ZERO; // Could choose to skip trade or round up to min lot size
        }

        if (WHOLE_QUANTITY_ONLY) {
            return quantity.divideToIntegralValue(BigDecimal.ONE);
        }
        return quantity;
    }
}

