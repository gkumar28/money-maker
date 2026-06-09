package sre.engine.strategy.rule;

import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

public class WithinPercentageRule implements Rule {

    private final Indicator<Num> referenceIndicator;
    private final Indicator<Num> targetIndicator;
    private final Num percentage;
    private final BarSeries series;
    private final NumFactory numFactory;

    public WithinPercentageRule(Indicator<Num> referenceIndicator, Number threshold, double percentage) {
        this.series = referenceIndicator.getBarSeries();
        this.referenceIndicator = referenceIndicator;
        this.numFactory = this.series.numFactory();
        this.targetIndicator = new ConstantIndicator<>(series, numFactory.numOf(threshold));
        this.percentage = numFactory.numOf(percentage / 100.0); // convert to decimal
    }

    public WithinPercentageRule(Indicator<Num> referenceIndicator, Indicator<Num> targetIndicator, double percentage) {
        this.series = referenceIndicator.getBarSeries();
        this.numFactory = this.series.numFactory();
        this.referenceIndicator = referenceIndicator;
        this.targetIndicator = targetIndicator;
        this.percentage = numFactory.numOf(percentage / 100.0); // convert to decimal
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        Num referenceValue = referenceIndicator.getValue(index);
        Num targetValue = targetIndicator.getValue(index);

        Num lowerBound = targetValue.multipliedBy(numFactory.numOf(1).minus(percentage));
        Num upperBound = targetValue.multipliedBy(numFactory.numOf(1).plus(percentage));

        return referenceValue.isGreaterThanOrEqual(lowerBound) &&
            referenceValue.isLessThanOrEqual(upperBound);
    }
}
