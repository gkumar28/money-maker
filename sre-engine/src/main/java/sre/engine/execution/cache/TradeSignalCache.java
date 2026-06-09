package sre.engine.execution.cache;

import lombok.Data;
import org.springframework.stereotype.Component;
import sre.engine.execution.constant.enums.TradeSignal;
import sre.engine.execution.schemaobject.SignalState;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class TradeSignalCache {

    private final ConcurrentHashMap<String, SignalState> cache = new ConcurrentHashMap<>();

    public SignalState get(String instrument) {
        return cache.computeIfAbsent(instrument, key -> new SignalState(TradeSignal.HOLD, ZonedDateTime.now(), null));
    }

    public void update(String instrument, SignalState newState) {
        cache.put(instrument, newState);
    }
}
