package strategy.engine.cache;

import strategy.engine.schemaobject.Signal;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class TradeSignalCache {

    private final ConcurrentHashMap<String, Signal> cache = new ConcurrentHashMap<>();

    public Signal get(String instrument) {
        return cache.computeIfAbsent(instrument, key -> new Signal(null, ZonedDateTime.now()));
    }

    public void update(String instrument, Signal newState) {
        cache.put(instrument, newState);
    }
}
