package money.maker.strategy.impl;

import money.maker.strategy.TradingStrategy;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

public class TrendStrategy implements TradingStrategy {
    @Override
    public Strategy build(BarSeries barSeries) {
        return null;
    }

    @Override
    public TradingRecord backTest(BarSeries barSeries) {
        return null;
    }
}
