package sre.engine.execution.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import sre.engine.execution.schemaobject.Tick;
import sre.engine.execution.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TickDataHandler {

    private  final ObjectMapper objectMapper;
    private final OrderService orderService;

    public void handleMessage(String message, String channel) {
        try {
            String instrument = extractInstrumentName(channel);
            Tick data = objectMapper.readValue(message, Tick.class);
            log.debug("received new bar event for instrument {} : {}", instrument, message);
            orderService.handle(data);
        } catch (Exception exception) {
            log.error("error while processing bar for channel {} : {} ", channel, message);
        }

    }

    private String extractInstrumentName(String channel) {
        String[] channelTokens = channel.split("\\.");
        return channelTokens[2];
    }
}