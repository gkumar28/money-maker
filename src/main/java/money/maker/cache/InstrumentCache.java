package money.maker.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

@Component
@Slf4j
@RequiredArgsConstructor
public class InstrumentCache {

    private final Cache<String, BarSeries> cache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .build();

    public void clearCache() {
        cache.invalidateAll();
    }

    public BarSeries get(String key) {
        return cache.getIfPresent(key);
    }

    public BarSeries getOrCreateInstrumentData(String token) {
        try {
            return cache.get(token, BaseBarSeries::new);
        } catch (Exception e) {
            log.error("Error getting/creating InstrumentData for token: {}", token, e);
            return null;
        }
    }

    public void updateInstrument(String token, Bar bar) {
        BarSeries instrumentData = getOrCreateInstrumentData(token);
        instrumentData.addBar(bar);
    }


}
