package sre.engine.strategy.schemaobject.barseries;

import org.ta4j.core.Bar;
import org.ta4j.core.BarBuilderFactory;
import org.ta4j.core.bars.TimeBarBuilderFactory;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.NumFactory;
import sre.engine.strategy.schemaobject.barseries.provider.BarDataProvider;

import java.util.ArrayList;
import java.util.List;

public class LookAheadBarSeriesBuilder {

    private static final String UNNAMED_SERIES_NAME = "unnamed_series";

    private List<Bar> bars;
    private String name;
    private boolean constrained;

    private int maxBarCount;
    private int fetchCount;

    private int seriesBeginIndex;
    private int seriesEndIndex;

    private boolean isNumFactoryAssigned = false;
    private NumFactory numFactory = DecimalNumFactory.getInstance();
    private BarBuilderFactory barBuilderFactory = new TimeBarBuilderFactory();
    private BarDataProvider dataProvider;

    public LookAheadBarSeriesBuilder() {
        initValues();
    }

    private void initValues() {
        this.bars = new ArrayList<>();
        this.name = UNNAMED_SERIES_NAME;
        this.constrained = false;
        this.maxBarCount = Integer.MAX_VALUE;
        this.fetchCount = Integer.MAX_VALUE;
        this.seriesBeginIndex = 0;
        this.seriesEndIndex = -1;
        this.dataProvider = null;
    }

    public LookAheadBarSeries build() {

        if (dataProvider == null) {
            throw new IllegalStateException("Data Provider must be specified");
        }

        if (seriesEndIndex < seriesBeginIndex) {
            throw new IllegalStateException(
                    "seriesEndIndex must be >= seriesBeginIndex");
        }

        if (!bars.isEmpty()) {

            if (!isNumFactoryAssigned) {
                numFactory = bars.getFirst().numFactory();
            }

            for (Bar bar : bars) {

                if (bar.getClosePrice() != null
                        && !numFactory.produces(bar.getClosePrice())) {

                    throw new IllegalArgumentException(
                            String.format(
                                    "Cannot add Bar with data type %s to series with datatype %s",
                                    bar.getClosePrice().getClass(),
                                    numFactory.one().getClass()));
                }
            }
        }

        int effectiveFetchCount =
                fetchCount == Integer.MAX_VALUE
                        ? maxBarCount
                        : fetchCount;

        LookAheadBarSeries series =
                new LookAheadBarSeries(
                        name == null ? UNNAMED_SERIES_NAME : name,
                        bars,
                        barBuilderFactory,
                        numFactory,
                        constrained,
                        dataProvider,
                        effectiveFetchCount,
                        seriesBeginIndex,
                        seriesEndIndex,
                        Math.max(0, seriesBeginIndex),
                        maxBarCount
                );

        initValues();

        return series;
    }

    @Deprecated(since = "0.22.2")
    public LookAheadBarSeriesBuilder setConstrained(boolean constrained) {
        this.constrained = constrained;
        return this;
    }

    public LookAheadBarSeriesBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public LookAheadBarSeriesBuilder withBars(List<Bar> bars) {
        this.bars = bars;
        return this;
    }

    public LookAheadBarSeriesBuilder withNumFactory(NumFactory numFactory) {

        if (numFactory != null) {
            isNumFactoryAssigned = true;
        }

        this.numFactory = numFactory;
        return this;
    }

    public LookAheadBarSeriesBuilder withBarBuilderFactory(
            BarBuilderFactory barBuilderFactory) {

        this.barBuilderFactory = barBuilderFactory;
        return this;
    }

    public LookAheadBarSeriesBuilder withMaxBarCount(int maxBarCount) {
        this.maxBarCount = maxBarCount;
        return this;
    }

    public LookAheadBarSeriesBuilder withFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
        return this;
    }

    public LookAheadBarSeriesBuilder withSeriesBeginIndex(int seriesBeginIndex) {
        this.seriesBeginIndex = seriesBeginIndex;
        return this;
    }

    public LookAheadBarSeriesBuilder withSeriesEndIndex(int seriesEndIndex) {
        this.seriesEndIndex = seriesEndIndex;
        return this;
    }

    public LookAheadBarSeriesBuilder withDataProvider(
            BarDataProvider dataProvider) {

        this.dataProvider = dataProvider;
        return this;
    }
}