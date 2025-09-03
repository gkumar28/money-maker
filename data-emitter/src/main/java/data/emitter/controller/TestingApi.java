package data.emitter.controller;


import data.emitter.dto.Tick;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api")
public interface TestingApi {

    @PostMapping("/tick")
    ResponseEntity<String> sendTick(@RequestBody Tick tick);
}
