package execution.engine.controller;


import execution.engine.cache.InstrumentCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CacheApiController implements CacheApi {

    private final InstrumentCache instrumentCache;


    @Override
    public ResponseEntity<List<Bar>> getInstrumentData(String instrument) {
        BarSeries result = instrumentCache.get(instrument);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result.getBarData());
    }
}
