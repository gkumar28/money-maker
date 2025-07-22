package money.maker.service;

import money.maker.dto.Tick;

public interface CandleAggregatorService {

    void processTick(Tick tick);

    void updateInstrument(int longMaPeriod, int shortMaPeriod, int rsiPeriod);
}
