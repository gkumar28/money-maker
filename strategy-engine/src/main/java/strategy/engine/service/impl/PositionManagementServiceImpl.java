package strategy.engine.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import strategy.engine.constant.enums.TradeDirection;
import strategy.engine.schemaobject.SignalDto;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.service.PortfolioService;
import strategy.engine.service.PositionManagementService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionManagementServiceImpl implements PositionManagementService {

    private static final double MAX_CAPITAL_ALLOCATION_PCT = 0.02;  // 2% risk per trade
    private static final int MIN_LOT_SIZE = 1;

    // Entry position constants
    private static final double SL_MIN_MULTIPLIER = 1.0;
    private static final double SL_MAX_MULTIPLIER = 2.5;
    private static final double TP_MIN_MULTIPLIER = 2.0;
    private static final double TP_MAX_MULTIPLIER = 4.0;

    // Exit position constants
    private static final double MIN_CONFIDENCE_TO_SELL = 0.5;
    private static final double MIN_CONFIDENCE_TO_CLOSE = 0.8;
    private static final double MINIMUM_EXIT_POSITION_PCT = 0.2;

    // capital allocation constants
    private static final double CAPITAL_ALLOCATION_WEAK_TREND_MULTIPLIER = 0.5;
    private static final double CAPITAL_ALLOCATION_STRONG_TREND_MULTIPLIER = 1.2;

    private final ConcurrentHashMap<String, Integer> holdings = new ConcurrentHashMap<>();
    private final PortfolioService portfolioService;


    @Override
    public StrategyOrderDto createOrderForLongPosition(String instrument, SignalDto signal) {
        if (null == signal.getDirection()) {
            return StrategyOrderDto.empty(instrument, signal);
        }

        return switch (signal.getDirection()) {
            case BUY -> calculateLongPositionEntrySize(instrument, signal);
            case SELL -> calculateLongPositionExitSize(instrument, signal);
        };
    }

    @Override
    public StrategyOrderDto calculateLongPositionExitSize(String instrument, SignalDto signal) {
        int currentHoldings = portfolioService.getCurrentHoldings(instrument).getQuantity();

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
            return null;  // Don't place tiny sell orders
        }

        return new StrategyOrderDto(
            instrument,
            signal.getTimestamp(),
            TradeDirection.SELL,
            quantity,
            signal.getPrice(),
            null,
            null,
            signal.getConfidence(),
            signal.getPrice().multiply(BigDecimal.valueOf(quantity))  // capital released
        );
    }

    @Override
    public StrategyOrderDto calculateLongPositionEntrySize(String instrument, SignalDto signal) {
        // Base capital allocation (2% of account)
        BigDecimal baseCapital = portfolioService.getTotalValue().multiply(BigDecimal.valueOf(MAX_CAPITAL_ALLOCATION_PCT));
        BigDecimal finalCapitalAllocation = getFinalCapitalAllocation(signal, baseCapital);

        // 7. Calculate quantity = capital allocation / entry price
        int quantity = finalCapitalAllocation.divide(signal.getPrice(), 4, RoundingMode.HALF_UP).intValue();
        BigDecimal stopLoss = calculateStopLoss(signal);
        BigDecimal takeProfit = calculateTakeProfit(signal);
        if (quantity < MIN_LOT_SIZE) {
            quantity = 0; // Could choose to skip trade or round up to min lot size
        }

        return new StrategyOrderDto(
            instrument,
            signal.getTimestamp(),
            TradeDirection.BUY,
            quantity,
            signal.getPrice(),
            stopLoss,
            takeProfit,
            signal.getConfidence(),
            finalCapitalAllocation
        );
    }

    private BigDecimal getFinalCapitalAllocation(SignalDto signal, BigDecimal baseCapital) {
        // Volatility adjustment using ATR (inverse relation)
        // Avoid divide by zero or very small ATR values
        BigDecimal adjustedAtr = signal.getAtr().max(new BigDecimal("0.0001"));
        BigDecimal volatilityAdjustedCapital = baseCapital.divide(adjustedAtr, 4, RoundingMode.HALF_UP);

        BigDecimal trendMultiplier = BigDecimal.ONE;
        if (signal.getAdx().doubleValue() < 20) {
            trendMultiplier = BigDecimal.valueOf(CAPITAL_ALLOCATION_WEAK_TREND_MULTIPLIER);      // Weak trend → reduce size
        } else if (signal.getAdx().doubleValue() > 30) {
            trendMultiplier = BigDecimal.valueOf(CAPITAL_ALLOCATION_STRONG_TREND_MULTIPLIER);     // Strong trend → increase size
        }

        // Calculate final capital to allocate for this trade
        return volatilityAdjustedCapital.multiply(trendMultiplier).multiply(signal.getConfidence());
    }

    private BigDecimal calculateStopLoss(SignalDto signal) {
        // Example: use ATR (Average True Range) to set SL some multiple below entry price
        BigDecimal entryPrice = signal.getPrice();
        BigDecimal atr = signal.getAtr();
        BigDecimal confidence = signal.getConfidence();

        // Inverse relation: stronger signal => smaller multiplier
        double multiplier = SL_MAX_MULTIPLIER - (confidence.doubleValue() * (SL_MAX_MULTIPLIER - SL_MIN_MULTIPLIER));

        BigDecimal slDistance = atr.multiply(BigDecimal.valueOf(multiplier));
        BigDecimal stopLoss = entryPrice.subtract(slDistance);
        if (stopLoss.compareTo(BigDecimal.ZERO) < 0) {
            stopLoss = BigDecimal.ZERO;
        }
        return stopLoss.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTakeProfit(SignalDto signal) {
        BigDecimal entryPrice = signal.getPrice();
        BigDecimal atr = signal.getAtr();
        BigDecimal confidence = signal.getConfidence();

        // Direct relation: stronger signal => larger multiplier
        double multiplier = TP_MIN_MULTIPLIER + (confidence.doubleValue() * (TP_MAX_MULTIPLIER - TP_MIN_MULTIPLIER));

        BigDecimal tpDistance = atr.multiply(BigDecimal.valueOf(multiplier));
        BigDecimal takeProfit = entryPrice.add(tpDistance);
        return takeProfit.setScale(2, RoundingMode.HALF_UP);
    }
}

