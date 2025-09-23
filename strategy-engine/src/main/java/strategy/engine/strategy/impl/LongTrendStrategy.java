package strategy.engine.strategy.impl;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RecentSwingHighIndicator;
import org.ta4j.core.indicators.ZLEMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.TrailingStopLossRule;
import strategy.engine.adapter.TA4JAdapter;
import strategy.engine.constant.enums.TradeAction;
import strategy.engine.indicator.KallmanIndicator;
import strategy.engine.constant.enums.TradeType;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.analysis.TradingRecord;
import strategy.engine.strategy.TradingStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.math.BigDecimal;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public class LongTrendStrategy extends TradingStrategy {
    
    private ClosePriceIndicator close;
    private HighPriceIndicator high;
    private KallmanIndicator kallman;
    private ZLEMAIndicator ema10;
    private ZLEMAIndicator ema20;
    private ZLEMAIndicator ema50;
    private ADXIndicator adx30;
    private RSIIndicator rsi30;
    private ATRIndicator atr30;
    private MACDIndicator macd;
    private EMAIndicator macdSignal;
    private RecentSwingHighIndicator swingHigh;

    private Rule entryRule;
    private Rule expandRule;
    private Rule trimRule;
    private Rule exitRule;

    public LongTrendStrategy(BarSeries barSeries, TradingRecord tradingRecord) {
        super(barSeries, tradingRecord);
    }

    @Override
    protected void build() {

        close = new ClosePriceIndicator(barSeries);
        high = new HighPriceIndicator(barSeries);
        ema10 = new ZLEMAIndicator(close, 10);
        ema20 = new ZLEMAIndicator(close, 20);
        ema50 = new ZLEMAIndicator(close, 50);
        kallman = new KallmanIndicator(close, 0.01, 20);
        adx30 = new ADXIndicator(barSeries, 30);
        rsi30 = new RSIIndicator(close, 30);
        atr30 = new ATRIndicator(barSeries, 30);
        macd = new MACDIndicator(close, 24, 52);
        macdSignal = macd.getSignalLine(18);
        swingHigh = new RecentSwingHighIndicator(high, 50, 0, 10);

        // Entry rule - shows bullish direction
        entryRule = new CrossedUpIndicatorRule(macd, macdSignal)
            .and(new CrossedUpIndicatorRule(rsi30, 50));

        // Add rule - trend is going strong
        expandRule = new CrossedUpIndicatorRule(macd, macdSignal) // uptrend
            .and(new CrossedUpIndicatorRule(rsi30, 60))
            .or(new CrossedUpIndicatorRule(close, swingHigh));

        // Trim rule - trend weakening / partial trend reversal
        trimRule = new CrossedDownIndicatorRule(rsi30, 50)
            .and(new CrossedDownIndicatorRule(macd, macdSignal))
            .or(new CrossedDownIndicatorRule(close, swingHigh))
            .or(new TrailingStopLossRule(high, high.numOf(5)));

        // Exit rule - complete trend reversal
        exitRule = new CrossedDownIndicatorRule(rsi30, 40)
            .or(new StopLossRule(close, 5))
            .or(new TrailingStopLossRule(close, close.numOf(5)));
    }

    @Override
    public Signal evaluate(int index, Holding currentHolding) {

        org.ta4j.core.TradingRecord ta4jTradingRecord = TA4JAdapter.asTradingRecord(tradingRecord);
        BigDecimal currentQuantity = currentHolding.getQuantity();
        boolean quantityGreaterThanZero = currentQuantity.compareTo(BigDecimal.ZERO) > 0;
        boolean entry = entryRule.isSatisfied(index, ta4jTradingRecord) && !quantityGreaterThanZero;
        boolean add = expandRule.isSatisfied(index, ta4jTradingRecord) && quantityGreaterThanZero;
        boolean trim = trimRule.isSatisfied(index, ta4jTradingRecord) && quantityGreaterThanZero;
        boolean exit = exitRule.isSatisfied(index, ta4jTradingRecord) && quantityGreaterThanZero;

        TradeType tradeType = null;
        TradeAction tradeAction = null;

        if (index <= 50) {
            // do nothing as unstable
        } else if (entry) {
            tradeType = TradeType.BUY;
            tradeAction = TradeAction.ENTRY;
            logDebugMessage("ENTRY", index);
        } else if (add) {
            tradeType = TradeType.BUY;
            tradeAction = TradeAction.EXPAND;
            logDebugMessage("EXPAND", index);
        } else if (exit) {
            tradeType = TradeType.SELL;
            tradeAction = TradeAction.EXIT;
            logDebugMessage("EXIT", index);
        } else if (trim) {
            tradeType = TradeType.SELL;
            tradeAction = TradeAction.TRIM;
            logDebugMessage("TRIM", index);
        }

        return new Signal(
            tradeType,
            tradeAction,
            barSeries.getBar(index).getEndTime(),
            barSeries.getBar(index).getClosePrice().bigDecimalValue(),
            atr30.getValue(index).bigDecimalValue(),
            adx30.getValue(index).bigDecimalValue()
        );
    }

    private void logDebugMessage(String signal, int index) {
        if (log.isDebugEnabled()) {
            log.debug("{}: {} at index {}", tradingRecord.getName(), signal, index);
        }
    }

}
