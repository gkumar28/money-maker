package sre.engine.strategy.listener;

import org.ta4j.core.Bar;
import sre.engine.strategy.service.RedisService;
import sre.engine.strategy.service.SignalGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BarDataHandler {

    private final RedisService redisService;
    private final SignalGenerationService signalGenerationService;

    public void handleMessage(String message, String channel) {
        try {
            String instrument = extractInstrumentName(channel);

            log.debug("received new bar event for instrument {} : {}", instrument, message);
            Bar bar = redisService.getBar(instrument, message);
            signalGenerationService.onNewBarEvent(instrument, bar);
        } catch (Exception exception) {
            log.error("error while processing bar for channel {} : {} ", channel, message);
        }

    }

    private String extractInstrumentName(String channel) {
        String[] channelTokens = channel.split("\\.");
        return channelTokens[2];
    }
}
