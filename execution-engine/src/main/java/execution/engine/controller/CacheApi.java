package execution.engine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.ta4j.core.Bar;

import java.util.List;

@RequestMapping("/api")
public interface CacheApi {

    @GetMapping("/instrument/data")
    ResponseEntity<List<Bar>> getInstrumentData(@RequestParam String instrument);
}
