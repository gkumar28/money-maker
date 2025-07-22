package money.maker.controller;

import money.maker.dto.InstrumentData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/cache")
public interface CacheApi {

    @GetMapping("")
    ResponseEntity<InstrumentData> getCacheData(@RequestParam("key") String key);
}
