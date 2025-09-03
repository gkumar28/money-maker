package execution.engine.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public interface TradingStrategy {

    Strategy build(BarSeries barSeries);
}
