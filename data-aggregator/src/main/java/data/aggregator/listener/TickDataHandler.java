package data.aggregator.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.aggregator.dto.Tick;
import data.aggregator.service.TickAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TickDataHandler {

    private final ObjectMapper objectMapper;
    private final TickAggregatorService tickAggregatorService;

    public void handleMessage(String message, String channel) {
        try {
            Tick tick = objectMapper.readValue(message, Tick.class);
            String instrument = extractInstrumentName(channel);
            tick.setInstrument(instrument);

            log.debug("received tick event for instrument {} : {}", instrument, message);
            tickAggregatorService.processTick(tick);
        } catch (Exception exception) {
            log.error("error while processing tick in channel {} message : {}", channel, message);
        }

    }

    private String extractInstrumentName(String channel) {
        String[] channelTokens = channel.split("\\.");
        return channelTokens[2];
    }
}
