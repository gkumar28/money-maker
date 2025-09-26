package strategy.engine.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import strategy.engine.strategy.StrategyInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class StrategyInstanceRegistry {

    private final Map<String, StrategyInstance> strategyMap = new ConcurrentHashMap<>();

    public void register(String instrument, StrategyInstance strategy) {
        strategyMap.put(instrument, strategy);
    }

    public StrategyInstance get(String instrument) {
        return strategyMap.get(instrument);
    }

    public void unregister(String instrument) {
        strategyMap.remove(instrument);
    }

    public boolean isRegistered(String instrument) {
        return strategyMap.containsKey(instrument);
    }

    public void clearAll() {
        strategyMap.clear();
    }

    public Map<String, StrategyInstance> getAll() {
        return strategyMap;
    }
}
