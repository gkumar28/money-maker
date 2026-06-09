package sre.engine.strategy.strategy;

import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import sre.engine.strategy.schemaobject.signal.SignalContext;

public interface SignalStrategy extends ExtendedStrategy {

    default SignalContext operate(int index) {
        return operate(index, null);
    }

    SignalContext operate(int index, TradingRecord tradingRecord);

    @Override
    default Strategy and(Strategy strategy) {
        return and("and(" + this.getName() + "," + strategy.getName() + ")", strategy, Math.max(this.getUnstableBars(), strategy.getUnstableBars()));
    }

    @Override
    default Strategy or(Strategy strategy) {
        return or("or(" + this.getName() + "," + strategy.getName() + ")", strategy, Math.max(this.getUnstableBars(), strategy.getUnstableBars()));
    }
}
