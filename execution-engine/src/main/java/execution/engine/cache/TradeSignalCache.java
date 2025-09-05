package execution.engine.cache;

import execution.engine.constant.enums.TradeSignal;
import execution.engine.schemaobject.SignalState;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class TradeSignalCache {

    private final ConcurrentHashMap<String, SignalState> cache;

    public SignalState get(String instrument, String strategyName) {
        return cache.computeIfAbsent(instrument, key -> new SignalState(TradeSignal.HOLD, ZonedDateTime.now(), null, strategyName));
    }

    public void update(String instrument, TradeSignal signal, ZonedDateTime timestamp, BigDecimal price, String strategyName) {
        SignalState state = new SignalState(signal, timestamp, price, strategyName);
        cache.put(instrument, state);
    }
}
