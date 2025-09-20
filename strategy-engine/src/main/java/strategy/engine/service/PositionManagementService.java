package strategy.engine.service;

import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.Order;

import java.math.BigDecimal;

public interface PositionManagementService {

    Order calculateLongPositionEntrySize(String instrument, Signal signal);

    Order calculateLongPositionExitSize(String instrument, Signal signal);

    Order createOrderForLongPosition(String instrument, Signal signal);

    Order triggerSLTPForPosition(String instrument, Signal signal, BigDecimal currentPrice);

    void updateSlTpForInstrument(String instrument);
}
