package sre.engine.strategy.service;

import sre.engine.strategy.schemaobject.Portfolio;
import sre.engine.strategy.schemaobject.signal.SignalContext;

public interface RiskManagementService {

    SignalContext createOrder(Portfolio portfolio, String instrument, SignalContext signalContext);

    SignalContext triggerSLTP(Portfolio portfolio, String instrument, SignalContext signalContext);

    void updateSLTP(Portfolio portfolio, String instrument);
}
