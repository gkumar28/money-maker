package strategy.engine.cache;

import strategy.engine.schemaobject.SignalDto;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class TradeSignalCache {

    private final ConcurrentHashMap<String, SignalDto> cache = new ConcurrentHashMap<>();

    public SignalDto get(String instrument) {
        return cache.computeIfAbsent(instrument, key -> new SignalDto(null, ZonedDateTime.now()));
    }

    public void update(String instrument, SignalDto newState) {
        cache.put(instrument, newState);
    }
}
