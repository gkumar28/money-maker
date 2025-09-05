package strategy.engine.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import strategy.engine.strategy.TradingStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class TradingStrategyRegistry {

    private final Map<String, TradingStrategy> strategyMap = new ConcurrentHashMap<>();

    public void register(String instrument, TradingStrategy strategy) {
        strategyMap.put(instrument, strategy);
    }

    public TradingStrategy get(String instrument) {
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

    public Map<String, TradingStrategy> getAll() {
        return strategyMap;
    }
}
