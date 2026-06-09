package sre.engine.strategy.schemaobject.barseries.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.ta4j.core.Bar;
import org.ta4j.core.BarBuilder;
import org.ta4j.core.BarBuilderFactory;

import java.util.List;

@RequiredArgsConstructor
@Getter
public abstract class BarDataProvider {
    public abstract List<Bar> fetchBars(int startIndex, int count, BarBuilder barBuilder);

    public abstract int getDataSize();
}
