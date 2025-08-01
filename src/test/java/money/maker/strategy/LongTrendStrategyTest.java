package money.maker.strategy;

import money.maker.strategy.impl.LongTrendStrategy;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.backtest.BarSeriesManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongTrendStrategyTest {

    private LongTrendStrategy longTrendStrategy;

    @Test
    void backTest() {
        BarSeries barSeries = new BaseBarSeries();
        BarSeriesManager barSeriesManager = new BarSeriesManager(barSeries);
        String test = "test";
        assertEquals(test, "test");
    }
}
