package money.maker.rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

@RequiredArgsConstructor
public class SurgeRule implements Rule {

    private final Indicator<Num> targetIndicator;
    private final Indicator<Num> thresholdIndicator;
    private final Num multiplier;
    private final Num thresholdBars;

    public SurgeRule(Indicator<Num> targetIndicator, Indicator<Num> thresholdIndicator, Num multiplier) {
        this.targetIndicator = targetIndicator;
        this.thresholdIndicator = thresholdIndicator;
        this.multiplier = multiplier;
        this.thresholdBars = DoubleNum.valueOf(1);
    }


    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        if (thresholdBars.isEqual(DoubleNum.valueOf(1))) {
            return isSatisfiedAtIndex(index);
        }

        int startIndex = index - thresholdBars.intValue() + 1;
        for (int i=startIndex;i<=index;i++) {
            if (!isSatisfiedAtIndex(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSatisfiedAtIndex(int index) {
        return targetIndicator.getValue(index)
            .isGreaterThan(thresholdIndicator.getValue(index).multipliedBy(multiplier));
    }
}
