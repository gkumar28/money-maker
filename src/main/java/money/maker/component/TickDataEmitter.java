package money.maker.component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class TickDataEmitter {

    private final JsonFactory jsonFactory;
    private final Portfolio portfolio;
    private final TickDisruptorEngine tickDisruptorEngine;

    public void emit(String message) throws IOException {

        try (JsonParser parser = jsonFactory.createParser(message)) {
            String symbol = "";
            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();

                if (JsonToken.FIELD_NAME.equals(token) && "symbol".equals(parser.getCurrentName())) {
                    parser.nextToken();
                    symbol = parser.getValueAsString();
                    break;
                }
            }

            if (portfolio.getInstruments().contains(symbol)) {
                tickDisruptorEngine.publish(message);
            }
        }
    }
}
