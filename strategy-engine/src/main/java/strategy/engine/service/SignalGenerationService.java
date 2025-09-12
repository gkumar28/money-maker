package strategy.engine.service;

import org.ta4j.core.Bar;

public interface SignalGenerationService {

    void onNewBarEvent(String instrument, Bar bar);
}
