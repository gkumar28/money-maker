package strategy.engine.indicator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

@Slf4j
@RequiredArgsConstructor
public class ScaledIndicator implements Indicator<Num> {

    private final Indicator<Num> targetIndicator;
    private final Num scale;

    @Override
    public Num getValue(int index) {
        return targetIndicator.getValue(index).multipliedBy(scale);
    }

    @Override
    public int getUnstableBars() {
        return targetIndicator.getUnstableBars();
    }

    @Override
    public BarSeries getBarSeries() {
        return targetIndicator.getBarSeries();
    }
}
