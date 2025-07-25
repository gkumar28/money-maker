package money.maker.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.dto.Tick;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketDataDispatcher {

    private final ObjectMapper objectMapper;
    private final ProducerTemplate producerTemplate;
    private final RiskManager riskManager;

    public void sendMessage(String message) throws JsonProcessingException {
        Tick data = objectMapper.readValue(message, Tick.class);
        if (riskManager.getInstruments().contains(data.getInstrumentToken())) {
            producerTemplate.sendBody("direct:websocket-data", data);
        }
    }
}
