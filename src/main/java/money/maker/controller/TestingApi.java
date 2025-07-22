package money.maker.controller;


import money.maker.dto.Tick;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/mock")
public interface TestingApi {

    @PostMapping("/sendTick")
    ResponseEntity<String> sendTick(@RequestBody Tick tick);
}
