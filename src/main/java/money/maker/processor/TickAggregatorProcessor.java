package money.maker.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.service.CandleAggregatorService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import money.maker.dto.Tick;

@Component
@RequiredArgsConstructor
@Slf4j
public class TickAggregatorProcessor implements Processor {

    private final CandleAggregatorService candleAggregatorService;

    @Override
    public void process(Exchange exchange) throws Exception {
        Tick currentTick = (Tick) exchange.getMessage().getBody();
        log.debug("data received in tickAggregator: {}", currentTick);

        candleAggregatorService.processTick(currentTick);
    }
}
