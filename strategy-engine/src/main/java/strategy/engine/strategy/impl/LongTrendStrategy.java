package strategy.engine.strategy.impl;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RecentSwingHighIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.candles.BullishEngulfingIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.rules.AndRule;
import org.ta4j.core.rules.AverageTrueRangeStopGainRule;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.IsRisingRule;
import org.ta4j.core.rules.NotRule;
import org.ta4j.core.rules.OrRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.TrailingStopLossRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import strategy.engine.adapter.TA4JAdapter;
import strategy.engine.constant.enums.TradeAction;
import strategy.engine.constant.enums.TradeType;
import strategy.engine.indicator.ScaledIndicator;
import strategy.engine.rule.WithinPercentageRule;
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
    private LowPriceIndicator low;
    private VolumeIndicator volume;
    private EMAIndicator avgVolume;
    private ADXIndicator adx60;
    private RSIIndicator rsi60;
    private ATRIndicator atr60;
    private MACDIndicator macd;
    private EMAIndicator macdSignal;
    private RecentSwingHighIndicator swingHigh;
    private HighestValueIndicator resistance;

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
        low = new LowPriceIndicator(barSeries);
        volume = new VolumeIndicator(barSeries);
        avgVolume = new EMAIndicator(volume, 600);
        adx60 = new ADXIndicator(barSeries, 600);
        rsi60 = new RSIIndicator(close, 600);
        atr60 = new ATRIndicator(barSeries, 600);
        macd = new MACDIndicator(close, 360, 840);
        macdSignal = macd.getSignalLine(180);
        swingHigh = new RecentSwingHighIndicator(high, 600, 120, 120);
        resistance = new HighestValueIndicator(high, 4320);
        BullishEngulfingIndicator bullishEngulfing = new BullishEngulfingIndicator(barSeries);

        Rule priceActionRule = new IsRisingRule(high, 60, 0.6)
            .and(new IsRisingRule(low, 60, 0.6))
            .and(new UnderIndicatorRule(rsi60, 60))
            .and(new OverIndicatorRule(rsi60, 45))
            .and(new OverIndicatorRule(adx60, 25));

        Rule indicatorRule = new OrRule(
            new CrossedUpIndicatorRule(macd, macdSignal),
            new CrossedUpIndicatorRule(close, resistance)
            .and(new NotRule(new WithinPercentageRule(resistance, swingHigh, 5))))
            .and(new OverIndicatorRule(rsi60, 45))
            .and(new UnderIndicatorRule(rsi60, 60))
            .and(new OverIndicatorRule(adx60, 25));

        // Entry rule - shows bullish direction
        entryRule = new OrRule(indicatorRule, priceActionRule);

        // Add rule - trend is going strong
        expandRule = new OverIndicatorRule(close, new ScaledIndicator(swingHigh, barSeries.numOf(1.1)))
            .and(new OverIndicatorRule(rsi60, 50))
            .and(new OverIndicatorRule(adx60, 30));

        // Trim rule - trend weakening / partial trend reversal
        trimRule = new AndRule(
            new NotRule(new WithinPercentageRule(resistance, swingHigh, 1.2)),
            new CrossedUpIndicatorRule(close, new ScaledIndicator(swingHigh, barSeries.numOf(0.98))))
            .or(new AverageTrueRangeStopGainRule(barSeries, close, 600, 2));

        // Exit rule - complete trend reversal
        exitRule = new CrossedDownIndicatorRule(rsi60, 40)
            .or(new TrailingStopLossRule(close,barSeries.numOf(5)))
            .or(new StopGainRule(close, 10));
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
            atr60.getValue(index).bigDecimalValue(),
            adx60.getValue(index).bigDecimalValue()
        );
    }

    private void logDebugMessage(String signal, int index) {
        if (log.isDebugEnabled()) {
            log.debug("{}: {} at index {}", tradingRecord.getName(), signal, index);
        }
    }

}
