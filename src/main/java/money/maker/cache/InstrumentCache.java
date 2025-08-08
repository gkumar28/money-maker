package money.maker.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DoubleNum;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class InstrumentCache {

    private final ConcurrentHashMap<String, BarSeries> cache = new ConcurrentHashMap<>();

    public void clearCache() {
        cache.clear();
    }

    public BarSeries get(String token) {
        return cache.computeIfAbsent(token,
            k -> {
            BarSeries barSeries = new BaseBarSeries(token, new ArrayList<>(), DoubleNum.ZERO);
            barSeries.setMaximumBarCount(100);

            return barSeries;
        });
    }

    public void updateInstrument(String token, Bar bar) {
        BarSeries instrumentData = get(token);
        instrumentData.addBar(bar);
    }


}
