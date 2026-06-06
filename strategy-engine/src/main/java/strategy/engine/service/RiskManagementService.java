package strategy.engine.service;

import strategy.engine.schemaobject.Portfolio;
import strategy.engine.schemaobject.signal.Signal;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.signal.SignalContext;

import java.math.BigDecimal;

public interface RiskManagementService {

    SignalContext createOrder(Portfolio portfolio, String instrument, SignalContext signalContext);

    SignalContext triggerSLTP(Portfolio portfolio, String instrument, SignalContext signalContext);

    void updateSLTP(Portfolio portfolio, String instrument);
}
