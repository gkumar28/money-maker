package execution.engine.strategy.impl;

import execution.engine.Indicator.KallmanIndicator;
import execution.engine.rule.SurgeRule;
import execution.engine.strategy.TradingStrategy;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

@Component
public class LongTrendStrategy extends TradingStrategy {

    @Override
    public void build(BarSeries barSeries) {
        ClosePriceIndicator close = new ClosePriceIndicator(barSeries);

        KallmanIndicator kallman = new KallmanIndicator(close, 0.01, 20);
        MACDIndicator macd = new MACDIndicator(close, 12, 26);
        EMAIndicator macdSignal = new EMAIndicator(macd, 9);

        ADXIndicator adx = new ADXIndicator(barSeries, 14);
        RSIIndicator rsi = new RSIIndicator(close, 14);

        VolumeIndicator volume = new VolumeIndicator(barSeries);
        SMAIndicator avgVolume = new SMAIndicator(volume, 20);

        // Price action: breakout above highest high of last N bars
        HighestValueIndicator recentHigh = new HighestValueIndicator(new HighPriceIndicator(barSeries), 20);

        // Entry rule
        Rule entryRule = new SurgeRule(close, kallman, DecimalNum.valueOf(1), DecimalNum.valueOf(10))
            .and(new SurgeRule(volume, avgVolume, DecimalNum.valueOf(1.2), DecimalNum.valueOf(10)))
            .and(new CrossedUpIndicatorRule(macd, macdSignal))
            .and(new OverIndicatorRule(adx, DecimalNum.valueOf(25)))
            .and(new OverIndicatorRule(rsi, DecimalNum.valueOf(35)))
            .and(new UnderIndicatorRule(rsi, DecimalNum.valueOf(65)))
            .and(new OverIndicatorRule(close, recentHigh));

        // Exit rule
        Rule exitRule = new SurgeRule(volume, avgVolume, DecimalNum.valueOf(1.2), DecimalNum.valueOf(10))
            .and(
                new CrossedDownIndicatorRule(macd, macdSignal)
                .or(new OverIndicatorRule(rsi, DecimalNum.valueOf(70)))
                .or(new UnderIndicatorRule(adx, DecimalNum.valueOf(20)))
            );

        this.strategy =  new BaseStrategy(this.getClass().getName(), entryRule, exitRule);
    }

    @Override
    public boolean shouldEnter() {
        isReady(this.getClass().getName());
        return strategy.shouldEnter(barSeries.getEndIndex());
    }

    @Override
    public boolean shouldExit() {
        isReady(this.getClass().getName());
        return strategy.shouldExit(barSeries.getEndIndex());
    }
}
