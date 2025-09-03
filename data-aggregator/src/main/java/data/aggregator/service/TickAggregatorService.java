package data.aggregator.service;

import data.aggregator.dto.Tick;

public interface TickAggregatorService {

    void processTick(Tick tick);

    void updateInstrument();
}
