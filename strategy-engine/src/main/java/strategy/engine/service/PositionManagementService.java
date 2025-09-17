package strategy.engine.service;

import strategy.engine.schemaobject.SignalDto;
import strategy.engine.schemaobject.StrategyOrderDto;

import java.math.BigDecimal;

public interface PositionManagementService {

    StrategyOrderDto calculateLongPositionEntrySize(String instrument, SignalDto signal);

    StrategyOrderDto calculateLongPositionExitSize(String instrument, SignalDto signal);

    StrategyOrderDto createOrderForLongPosition(String instrument, SignalDto signal);

    StrategyOrderDto triggerSLTPForPosition(String instrument, SignalDto signalDto, BigDecimal currentPrice);

    void updateSlTpForInstrument(String instrument);
}
