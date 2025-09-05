package execution.engine.listener;

import execution.engine.cache.TradeSignalCache;
import execution.engine.constant.enums.TradeSignal;
import execution.engine.schemaobject.SignalState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class SignalGenerationHandler {

    private final TradeSignalCache tradeSignalCache;

    public void handleMessage(String message, String channel) {
        try {
            String instrument = extractInstrumentName(channel);
            SignalState data = convertToSignal(message);
            log.debug("new signal for instrument {} : {}", instrument, message);
            tradeSignalCache.update(instrument, data);
        } catch (Exception exception) {
            log.error("error while processing bar for channel {} : {} ", channel, message);
        }

    }

    private String extractInstrumentName(String channel) {
        String[] channelTokens = channel.split("\\.");
        return channelTokens[2];
    }

    private SignalState convertToSignal(String message) {
        try {
            String[] parts = message.split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid signal string format");
            }

            TradeSignal signal = TradeSignal.valueOf(parts[0]);
            long timestampMillis = Long.parseLong(parts[1]);
            BigDecimal price = new BigDecimal(parts[2]);

            ZonedDateTime timestamp = Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.of("UTC"));

            return new SignalState(signal, timestamp, price);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SignalState from string: " + message, e);
        }
    }
}
