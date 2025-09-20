package strategy.engine.strategy;

import lombok.Data;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Signal;

@Data
public abstract class TradingStrategy {

    protected final BarSeries barSeries;
    protected final TradingRecord tradingRecord;

    protected TradingStrategy(BarSeries barSeries, TradingRecord tradingRecord) {
        this.barSeries = barSeries;
        this.tradingRecord = tradingRecord;
        build();
    }

    protected abstract void build();

    public abstract Signal evaluate(int index, Holding holding);
}
