package money.maker.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

public interface TradingStrategy {

    Strategy build(BarSeries barSeries);

    TradingRecord backTest(BarSeries barSeries);
}
