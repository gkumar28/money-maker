package data.aggregator.service;

import org.ta4j.core.Bar;

public interface RedisService {

    void raiseNewBarEvent(String instrument, Bar bar);
}
