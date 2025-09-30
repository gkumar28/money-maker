package strategy.engine.service;

import strategy.engine.schemaobject.Portfolio;
import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.Order;

import java.math.BigDecimal;

public interface PositionManagementService {

    Order calculateLongPositionEntrySize(Portfolio portfolio, String instrument, Signal signal);

    Order calculateLongPositionExitSize(Portfolio portfolio, String instrument, Signal signal);

    Order createOrderForLongPosition(Portfolio portfolio, String instrument, Signal signal);

    Order triggerSLTPForPosition(Portfolio portfolio, String instrument, Signal signal, BigDecimal currentPrice);

    void updateSlTpForInstrument(Portfolio portfolio, String instrument);
}
