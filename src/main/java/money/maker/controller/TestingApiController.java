package money.maker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.dev.WebSocketApiServer;
import money.maker.dto.Tick;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class TestingApiController implements TestingApi {

    private final WebSocketApiServer server;
    private final ObjectMapper objectMapper;

    public ResponseEntity<String> sendTick(@RequestBody Tick tick) {
        try {
            tick.setTimestamp(Instant.now().toEpochMilli());
            String json = objectMapper.writeValueAsString(tick);
            log.info("Broadcasting Tick: {}", json);
            server.broadcastTick(json);
            return ResponseEntity.ok("Tick sent to WebSocket clients");
        } catch (Exception e) {
            log.error("Failed to serialize or send Tick", e);
            return ResponseEntity.status(500).body("Error sending Tick");
        }
    }
}
