package money.maker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.ta4j.core.BarSeries;

@RequestMapping("/cache")
public interface CacheApi {

    @GetMapping("")
    ResponseEntity<BarSeries> getCacheData(@RequestParam("key") String key);

    @PostMapping("/clear")
    ResponseEntity<String> clearCacheData();
}
