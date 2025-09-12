package strategy.engine.strategy;

import lombok.Data;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import strategy.engine.schemaobject.SignalDto;

@Data
public abstract class TradingStrategy {

    protected Strategy strategy;
    protected BarSeries barSeries;

    protected TradingStrategy(BarSeries barSeries) {
        this.barSeries = barSeries;
        build();
    }

    protected abstract void build();

    public abstract SignalDto evaluate(int index);
}
