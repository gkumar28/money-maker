package execution.engine.strategy;

import lombok.Data;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

@Data
public abstract class TradingStrategy {

    protected Strategy strategy;
    protected BarSeries barSeries;

    public abstract void build(BarSeries barSeries);

    public abstract boolean shouldEnter();

    public abstract boolean shouldExit();

    protected void isReady(String strategyName) throws IllegalStateException {
        if (null == strategy) {
            throw new IllegalStateException(String.format("%s is not yet built.", strategyName));
        }
    }
}
