package strategy.engine.schemaobject.barseries;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BarBuilder;
import org.ta4j.core.BarBuilderFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.bars.TimeBarBuilderFactory;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.NumFactory;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LookAheadBarSeries implements BarSeries {

    @Serial
    private static final long serialVersionUID = -1878027091824789L;

    public LookAheadBarSeries(String name, BarDataProvider dataProvider, int seriesEndIndex, int maximumBarCount) {
        this(name, dataProvider, maximumBarCount, 0, seriesEndIndex, maximumBarCount);
    }

    public LookAheadBarSeries(String name, BarDataProvider dataProvider, int fetchCount, int seriesBeginIndex, int seriesEndIndex, int maximumBarCount) {
        this(name, DecimalNumFactory.getInstance(), false, dataProvider, fetchCount, seriesBeginIndex, seriesEndIndex, maximumBarCount);
    }

    public LookAheadBarSeries(String name, NumFactory numFactory, boolean constrained, BarDataProvider dataProvider, int fetchCount, int seriesBeginIndex, int seriesEndIndex, int maximumBarCount) {
        this(name, new ArrayList<>(), numFactory, constrained, dataProvider, fetchCount, seriesBeginIndex, seriesEndIndex, Math.max(0, seriesBeginIndex), maximumBarCount);
    }

    public LookAheadBarSeries(String name, List<Bar> bars, NumFactory numFactory, boolean constrained, BarDataProvider dataProvider, int fetchCount, int seriesBeginIndex, int seriesEndIndex, int cacheStartIndex, int maximumBarCount) {
        this(name, bars, new TimeBarBuilderFactory(), numFactory, constrained, dataProvider, fetchCount, seriesBeginIndex, seriesEndIndex, cacheStartIndex, maximumBarCount);
    }

    public LookAheadBarSeries(String name, List<Bar> bars, BarBuilderFactory barBuilderFactory, NumFactory numFactory, boolean constrained, BarDataProvider dataProvider, int fetchCount, int seriesBeginIndex, int seriesEndIndex, int cacheStartIndex, int maximumBarCount) {
        this.name = name;
        this.bars = bars;
        this.barBuilderFactory = barBuilderFactory;;
        this.numFactory = numFactory;
        this.constrained = constrained;
        this.dataProvider = dataProvider;
        this.fetchCount = fetchCount;
        this.seriesBeginIndex = seriesBeginIndex;
        this.seriesEndIndex = seriesEndIndex;
        this.cacheStartIndex = cacheStartIndex;
        this.maximumBarCount = maximumBarCount;
    }

    @FunctionalInterface
    public interface BarDataProvider {
        List<Bar> fetchBars(int startIndex, int count);
    }

    private final String name;
    private final List<Bar> bars;
    private final BarBuilderFactory barBuilderFactory;
    private final NumFactory numFactory;
    private final boolean constrained;

    private final BarDataProvider dataProvider;
    private final int fetchCount;

    private final int seriesBeginIndex;
    private final int seriesEndIndex;
    /**
     * Absolute index represented by bars.get(0).
     */
    private int cacheStartIndex = 0;

    private int maximumBarCount = Integer.MAX_VALUE;

    @Override
    public NumFactory numFactory() {
        return numFactory;
    }

    @Override
    public BarBuilder barBuilder() {
        return barBuilderFactory.createBarBuilder(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized Bar getBar(int index) {

        if (bars.isEmpty()) {
            loadInitialWindow(index);
        }

        // Requested before currently loaded window
        if (index < cacheStartIndex) {
            return bars.getFirst();
        }

        int lastLoadedIndex = cacheStartIndex + bars.size() - 1;

        // Requested inside current window
        if (index <= lastLoadedIndex) {
            return bars.get(index - cacheStartIndex);
        }

        // Requested beyond current window
        moveWindowToInclude(index);

        return bars.get(index - cacheStartIndex);
    }

    private void loadInitialWindow(int requestedIndex) {

        int startIndex = Math.max(0,
                requestedIndex - Math.min(maximumBarCount - 1, requestedIndex));

        List<Bar> fetched =
                dataProvider.fetchBars(startIndex, Math.max(fetchCount, maximumBarCount));

        if (fetched.isEmpty()) {
            throw new IndexOutOfBoundsException(
                    "Unable to load bar " + requestedIndex);
        }

        bars.clear();
        bars.addAll(fetched);

        trimToMaximum();
        cacheStartIndex = startIndex;
    }

    private void moveWindowToInclude(int requestedIndex) {

        while (requestedIndex >= cacheStartIndex + bars.size()) {

            int nextFetchIndex = cacheStartIndex + bars.size();

            List<Bar> fetched = dataProvider.fetchBars(nextFetchIndex, fetchCount);

            if (fetched.isEmpty()) {
                throw new IndexOutOfBoundsException("Unable to fetch bar " + requestedIndex);
            }
            bars.addAll(fetched);
            trimToMaximum();
        }
    }

    private void trimToMaximum() {

        while (bars.size() > maximumBarCount) {
            bars.removeLast();
        }
    }

    @Override
    public int getBarCount() {
        return bars.size();
    }

    @Override
    public List<Bar> getBarData() {
        return Collections.unmodifiableList(bars);
    }

    @Override
    public int getBeginIndex() {
        return seriesBeginIndex;
    }

    @Override
    public int getEndIndex() {
        throw new UnsupportedOperationException("This series cannot reliably determine it's end index");
    }

    @Override
    public int getMaximumBarCount() {
        return maximumBarCount;
    }

    @Override
    public void setMaximumBarCount(int maximumBarCount) {

        if (maximumBarCount <= 0) {
            throw new IllegalArgumentException(
                    "Maximum bar count must be > 0");
        }

        this.maximumBarCount = maximumBarCount;

        trimToMaximum();
    }

    @Override
    public int getRemovedBarsCount() {
        return cacheStartIndex;
    }

    @Override
    public void addBar(Bar bar, boolean replace) {

        if (replace && !bars.isEmpty()) {
            bars.set(bars.size() - 1, bar);
        } else {
            bars.add(bar);
        }

        trimToMaximum();
    }

    @Override
    public void addTrade(Num tradeVolume, Num tradePrice) {

        if (bars.isEmpty()) {
            throw new IllegalStateException("No bars available");
        }

        bars.getLast().addTrade(tradeVolume, tradePrice);
    }

    @Override
    public void addPrice(Num price) {

        if (bars.isEmpty()) {
            throw new IllegalStateException("No bars available");
        }

        bars.getLast().addPrice(price);
    }

    @Override
    public BarSeries getSubSeries(int startIndex, int endIndex) {

        if (startIndex > endIndex) {
            throw new IllegalArgumentException(
                    "startIndex > endIndex");
        }

        List<Bar> subBars = new ArrayList<>();

        for (int i = startIndex; i <= endIndex; i++) {
            subBars.add(getBar(i));
        }

        LookAheadBarSeries subSeries =
                new LookAheadBarSeries(
                        name + "-sub",
                        subBars,
                        barBuilderFactory,
                        numFactory,
                        constrained,
                        dataProvider,
                        fetchCount,
                        seriesBeginIndex,
                        seriesEndIndex
                );

        subSeries.cacheStartIndex = startIndex;
        subSeries.maximumBarCount = endIndex - startIndex + 1;

        return subSeries;
    }
}