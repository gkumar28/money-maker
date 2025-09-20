package strategy.engine.strategy.impl;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopLossRule;
import org.ta4j.core.rules.TrailingStopLossRule;
import strategy.engine.constant.enums.TradeAction;
import strategy.engine.indicator.KallmanIndicator;
import strategy.engine.constant.enums.TradeDirection;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Signal;
import strategy.engine.strategy.TradingStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public class LongTrendStrategy extends TradingStrategy {
    
    private ClosePriceIndicator close;
    private KallmanIndicator kallman;
    private EMAIndicator ema10;
    private EMAIndicator ema20;
    private EMAIndicator ema50;
    private ADXIndicator adx14;
    private RSIIndicator rsi14;
    private ATRIndicator atr14;

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
        ema10 = new EMAIndicator(close, 10);
        ema20 = new EMAIndicator(close, 20);
        ema50 = new EMAIndicator(close, 50);
        kallman = new KallmanIndicator(close, 0.01, 20);
        adx14 = new ADXIndicator(barSeries, 14);
        rsi14 = new RSIIndicator(close, 14);
        atr14 = new ATRIndicator(barSeries, 14);

        // Entry rule - shows bullish direction
        entryRule = new OverIndicatorRule(ema10, ema50)
            .and(new OverIndicatorRule(close, ema20))
            .and(new CrossedUpIndicatorRule(rsi14, 40));

        // Add rule - trend is going strong
        expandRule = new OverIndicatorRule(close, ema10)
            .and(new OverIndicatorRule(ema10, ema20))
            .and(new OverIndicatorRule(rsi14, 55));

        // Trim rule - trend weakening / partial trend reversal
        trimRule = new CrossedDownIndicatorRule(rsi14, 60)
            .and(new OverIndicatorRule(close, ema20));

        // Exit rule - complete trend reversal
        exitRule = new CrossedDownIndicatorRule(close, ema20)
            .or(new CrossedDownIndicatorRule(rsi14, 45))
            .or(new StopLossRule(close, 10))
            .or(new TrailingStopLossRule(close, close.numOf(5), 100));
    }

    @Override
    public Signal evaluate(int index, Holding currentHolding) {

        int currentQuantity = currentHolding.getQuantity();
        boolean entry = entryRule.isSatisfied(index, tradingRecord) && currentQuantity == 0;
        boolean add = expandRule.isSatisfied(index, tradingRecord) && currentQuantity > 0;
        boolean trim = trimRule.isSatisfied(index, tradingRecord) && currentQuantity > 0;
        boolean exit = exitRule.isSatisfied(index, tradingRecord) && currentQuantity > 0;

        TradeDirection tradeDirection;
        TradeAction tradeAction;

        if (entry) {
            tradeDirection = TradeDirection.BUY;
            tradeAction = TradeAction.ENTRY;
            logDebugMessage("ENTRY", index);
        } else if (exit) {
            tradeDirection = TradeDirection.SELL;
            tradeAction = TradeAction.EXIT;
            logDebugMessage("EXIT", index);
        } else if (add) {
            tradeDirection = TradeDirection.BUY;
            tradeAction = TradeAction.EXPAND;
            logDebugMessage("EXPAND", index);
        } else if (trim) {
            tradeDirection = TradeDirection.SELL;
            tradeAction = TradeAction.TRIM;
            logDebugMessage("TRIM", index);
        }
        else {
            tradeDirection = null;
            tradeAction = null;
        }

        return new Signal(
            tradeDirection,
            tradeAction,
            barSeries.getBar(index).getEndTime(),
            barSeries.getBar(index).getClosePrice().bigDecimalValue(),
            atr14.getValue(index).bigDecimalValue(),
            adx14.getValue(index).bigDecimalValue()
        );
    }

    private void logDebugMessage(String signal, int index) {
        if (log.isDebugEnabled()) {
            log.debug("{}: {} at index {}", tradingRecord.getName(), signal, index);
        }
    }

}
