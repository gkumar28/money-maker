package execution.engine.listener;

import execution.engine.service.BarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BarDataHandler {

    private final BarService barService;

    public void handleMessage(String message, String channel) {
        try {
            String instrument = extractInstrumentName(channel);

            log.debug("received new bar event for instrument {} : {}", instrument, message);
            barService.onNewBarEvent(instrument, message);
        } catch (Exception exception) {
            log.error("error while processing bar for channel {} : {} ", channel, message);
        }

    }

    private String extractInstrumentName(String channel) {
        String[] channelTokens = channel.split("\\.");
        return channelTokens[2];
    }
}
