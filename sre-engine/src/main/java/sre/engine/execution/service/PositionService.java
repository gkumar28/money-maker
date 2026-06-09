package sre.engine.execution.service;

import sre.engine.execution.entity.Fill;
import sre.engine.execution.entity.Position;
import sre.engine.execution.schemaobject.SignalState;

import java.math.BigDecimal;

public interface PositionService {
    Position getOrCreateOpenPosition(String instrument);

    void handleSignal(String instrument, SignalState signal);

    void updatePositionWithFill(String instrument, Fill fill);

    void expandPosition(String instrument, BigDecimal fillQty, BigDecimal fillPrice);

    void trimPosition(String instrument, BigDecimal exitQty, BigDecimal exitPrice);
}
