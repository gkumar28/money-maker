package data.emitter.component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.emitter.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class TickDataEmitter {

    private final JsonFactory jsonFactory;
    private final Portfolio portfolio;
    private final RedisService redisService;

    @Async("taskExecutor")
    public void emit(String message) throws IOException {

        try (JsonParser parser = jsonFactory.createParser(message)) {
            String instrument = "";
            double price = 0.0;
            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();

                if (JsonToken.FIELD_NAME.equals(token) && "symbol".equals(parser.getCurrentName())) {
                    parser.nextToken();
                    instrument = parser.getValueAsString();
                }

                if (JsonToken.FIELD_NAME.equals(token) && "price".equals(parser.getCurrentName())) {
                    parser.nextToken();
                    price = parser.getValueAsDouble();
                }
            }

            if (portfolio.getInstruments().contains(instrument)) {
                redisService.updateInstrumentPrice(instrument, price);
                redisService.raiseTickEvent(instrument, message);
            }
        }
    }
}
