package strategy.engine.rule;

import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

public class WithinPercentageRule implements Rule {

    private final Indicator<Num> referenceIndicator;
    private final Indicator<Num> targetIndicator;
    private final Num percentage;
    private final BarSeries series;

    public WithinPercentageRule(Indicator<Num> referenceIndicator, Indicator<Num> targetIndicator, double percentage) {
        this.referenceIndicator = referenceIndicator;
        this.targetIndicator = targetIndicator;
        this.series = referenceIndicator.getBarSeries();
        this.percentage = series.numOf(percentage / 100.0); // convert to decimal
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        Num referenceValue = referenceIndicator.getValue(index);
        Num targetValue = targetIndicator.getValue(index);

        Num lowerBound = targetValue.multipliedBy(series.numOf(1).minus(percentage));
        Num upperBound = targetValue.multipliedBy(series.numOf(1).plus(percentage));

        return referenceValue.isGreaterThanOrEqual(lowerBound) &&
            referenceValue.isLessThanOrEqual(upperBound);
    }
}
