package sre.engine.strategy.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public  class  Registry<V> {

    private final Map<String, V> map = new ConcurrentHashMap<>();

    public void register(String key, V value) { map.put(key, value); }

    public V get(String key) { return map.get(key); }

    public void unregister(String key) { map.remove(key); }

    public boolean isRegistered(String key) { return map.containsKey(key); }

    public void clearAll() { map.clear(); }

    public int size() { return map.size(); }
    public Map<String, V> getAll() {
        return map;
    }
}
