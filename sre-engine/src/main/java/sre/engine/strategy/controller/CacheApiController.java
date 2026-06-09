package sre.engine.strategy.controller;


import sre.engine.strategy.cache.BarDataCache;
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

    private final BarDataCache barDataCache;


    @Override
    public ResponseEntity<List<Bar>> getInstrumentData(String instrument) {
        BarSeries result = barDataCache.get(instrument);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result.getBarData());
    }
}
