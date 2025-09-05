package execution.engine.service;

import execution.engine.entity.Fill;
import execution.engine.entity.Position;
import execution.engine.schemaobject.SignalState;

import java.math.BigDecimal;

public interface PositionService {
    Position getOrCreateOpenPosition(String instrument);

    void handleSignal(String instrument, SignalState signal);

    void updatePositionWithFill(String instrument, Fill fill);

    void expandPosition(String instrument, BigDecimal fillQty, BigDecimal fillPrice);

    void trimPosition(String instrument, BigDecimal exitQty, BigDecimal exitPrice);
}
