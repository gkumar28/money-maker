package money.maker.service;

import money.maker.dto.Tick;

public interface TickAggregatorService {

    void processTick(Tick tick);

    void updateInstrument();
}
