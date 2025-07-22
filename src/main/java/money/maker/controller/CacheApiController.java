package money.maker.controller;

import lombok.RequiredArgsConstructor;
import money.maker.cache.InstrumentCache;
import money.maker.dto.InstrumentData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CacheApiController implements CacheApi {

    private final InstrumentCache instrumentCache;

    @Override
    public ResponseEntity<InstrumentData> getCacheData(String key) {
        return ResponseEntity.ok(instrumentCache.get(key));
    }
}
