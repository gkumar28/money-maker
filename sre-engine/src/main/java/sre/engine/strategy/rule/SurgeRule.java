package sre.engine.strategy.rule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

@RequiredArgsConstructor
@Slf4j
public class SurgeRule implements Rule {

    private final Indicator<Num> targetIndicator;
    private final Indicator<Num> thresholdIndicator;
    private final Num multiplier;
    private final Num thresholdBars;


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        int startIndex = index - thresholdBars.intValue() + 1;
        if (startIndex < 0) return false;
        for (int i=startIndex;i<=index;i++) {
            if (!isSatisfiedAtIndex(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSatisfiedAtIndex(int index) {
        if (index < 0) return false;
        if (log.isTraceEnabled()) {
            log.trace("target: {} effective threshold: {} at index {}", targetIndicator.getValue(index).bigDecimalValue(), thresholdIndicator.getValue(index).multipliedBy(multiplier).bigDecimalValue(), index);
        }
        return targetIndicator.getValue(index)
            .isGreaterThanOrEqual(thresholdIndicator.getValue(index).multipliedBy(multiplier));
    }
}
