package strategy.engine.service;

import strategy.engine.schemaobject.SignalDto;
import strategy.engine.schemaobject.StrategyOrderDto;

public interface PositionManagementService {

    StrategyOrderDto calculateLongPositionEntrySize(String instrument, SignalDto signal);

    StrategyOrderDto createOrderForLongPosition(String instrument, SignalDto signal);

    StrategyOrderDto calculateLongPositionExitSize(String instrument, SignalDto signal);
}
