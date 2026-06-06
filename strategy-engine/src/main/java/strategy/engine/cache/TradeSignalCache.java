package strategy.engine.cache;

import strategy.engine.schemaobject.signal.Signal;
import lombok.Data;
import org.springframework.stereotype.Component;
import strategy.engine.schemaobject.signal.SignalContext;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class TradeSignalCache {

    private final ConcurrentHashMap<String, Signal> cache = new ConcurrentHashMap<>();

    public Signal get(String instrument) {
        return cache.computeIfAbsent(instrument, key -> Signal.instance());
    }

    public void update(String instrument, Signal newState) {
        cache.put(instrument, newState);
    }
}
