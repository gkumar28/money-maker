package execution.engine.cache;

import execution.engine.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class InstrumentCache {

    private final ConcurrentHashMap<String, BarSeries> cache = new ConcurrentHashMap<>();
    private final RedisService redisService;

    public void clearCache() {
        cache.clear();
    }

    public BarSeries get(String instrument) {
        return cache.computeIfAbsent(instrument,
            k -> {
            // get last 10 bars excluding current
            BarSeries barSeries = redisService.getNBars(instrument, 10, 1);
            barSeries.setMaximumBarCount(100);

            return barSeries;
        });
    }

    public void updateInstrument(String instrument, Bar bar) {
        BarSeries instrumentData = get(instrument);
        instrumentData.addBar(bar);
    }


}
