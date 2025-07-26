package money.maker.component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.ProducerType;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import money.maker.dto.Tick;
import money.maker.service.TickAggregatorService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class TickDisruptorEngine {

    private final Disruptor<TickEvent> disruptor;
    private final RingBuffer<TickEvent> ringBuffer;

    public TickDisruptorEngine(TaskExecutor taskExecutor,
                               TickAggregatorService tickAggregatorService,
                               ObjectMapper objectMapper) {
        int bufferSize = 4096; // must be power of 2

        disruptor = new Disruptor<>(
            new TickEventFactory(),
            bufferSize,
            taskExecutor,
            ProducerType.MULTI,
            new SleepingWaitStrategy()
        );

        disruptor.handleEventsWith(new TickEventHandler(tickAggregatorService, objectMapper));
        disruptor.start();

        ringBuffer = disruptor.getRingBuffer();
    }

    public void publish(String rawTickJson) {
        long seq = ringBuffer.next();
        try {
            TickEvent event = ringBuffer.get(seq);
            event.setTickStr(rawTickJson);
        } finally {
            ringBuffer.publish(seq);
        }
    }

    @PreDestroy
    public void shutdown() {
        disruptor.shutdown();
    }

    @Data
    private static class TickEvent {
        private String tickStr;
    }


    private static class TickEventFactory implements EventFactory<TickEvent> {
        @Override
        public TickEvent newInstance() {
            return new TickEvent();
        }
    }

    @RequiredArgsConstructor
    private static class TickEventHandler implements EventHandler<TickEvent> {

        private final TickAggregatorService tickAggregatorService;
        private final ObjectMapper objectMapper;

        @Override
        public void onEvent(TickEvent tickData, long l, boolean b) throws Exception {
            Tick tick = objectMapper.convertValue(tickData.getTickStr(), Tick.class);
            tickAggregatorService.processTick(tick);
        }
    }
}