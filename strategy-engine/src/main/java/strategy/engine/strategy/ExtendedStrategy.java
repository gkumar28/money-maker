package strategy.engine.strategy;

import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

public interface ExtendedStrategy extends Strategy {

    Rule getExpandRule();
    Rule getTrimRule();

    default boolean shouldExpand(int index) {
        return shouldExpand(index, null);
    }

    default boolean shouldExpand(int index, TradingRecord tradingRecord) {
        return !isUnstableAt(index) && getExpandRule().isSatisfied(index, tradingRecord);
    }

    default boolean shouldTrim(int index) {
        return shouldTrim(index, null);
    }

    default boolean shouldTrim(int index, TradingRecord tradingRecord) {
        return !isUnstableAt(index) && getTrimRule().isSatisfied(index, tradingRecord);
    }


}
