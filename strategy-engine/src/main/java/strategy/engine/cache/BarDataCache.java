package strategy.engine.cache;

import strategy.engine.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class BarDataCache {

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
            barSeries.setMaximumBarCount(50);

            return barSeries;
        });
    }

    public BarSeries updateAndGetInstrument(String instrument, Bar bar) {
        return cache.compute(instrument, (key, data) -> {
            if (null == data) {
                data = get(instrument);
            }
            data.addBar(bar);
            return data;
        });
    }


}
