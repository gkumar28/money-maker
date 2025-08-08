package money.maker.controller;

import lombok.RequiredArgsConstructor;
import money.maker.cache.InstrumentCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.BarSeries;

@RestController
@RequiredArgsConstructor
public class CacheApiController implements CacheApi {

    private final InstrumentCache instrumentCache;

    @Override
    public ResponseEntity<BarSeries> getCacheData(String key) {
        return ResponseEntity.ok(instrumentCache.get(key));
    }

    @Override
    public ResponseEntity<String> clearCacheData() {
        instrumentCache.clearCache();
        return ResponseEntity.ok("cache cleared successfully");
    }
}
