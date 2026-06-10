package sre.engine.strategy.service;

import common.lib.schemaobjects.Portfolio;
import sre.engine.strategy.schemaobject.signal.SignalContext;

public interface RiskManagementService {

    SignalContext createOrder(Portfolio portfolio, String instrument, SignalContext signalContext);

    SignalContext triggerSLTP(Portfolio portfolio, String instrument, SignalContext signalContext);

    void updateSLTP(Portfolio portfolio, String instrument);
}
