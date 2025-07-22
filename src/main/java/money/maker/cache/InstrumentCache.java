package money.maker.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.dto.Candle;
import money.maker.dto.InstrumentData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InstrumentCache {

    private final Cache<String, InstrumentData> cache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .build();

    public void clearCache() {
        cache.invalidateAll();
    }

    public InstrumentData get(String key) {
        return cache.getIfPresent(key);
    }

    public InstrumentData getOrCreateInstrumentData(String token, int longSmaPeriod, int shortEmaPeriod, int rsiPeriod) {
        try {
            return cache.get(token, () -> new InstrumentData(token, longSmaPeriod, shortEmaPeriod, rsiPeriod));
        } catch (Exception e) {
            log.error("Error getting/creating InstrumentData for token: {}", token, e);
            return null;
        }
    }

    public void updateInstrument(String token, Candle candle, int longSmaPeriod, int shortEmaPeriod, int rsiPeriod) {
        InstrumentData instrumentData = getOrCreateInstrumentData(token, longSmaPeriod, shortEmaPeriod, rsiPeriod);
        if (instrumentData != null) {
            instrumentData.updateIndicators(candle);
        }
    }


}
