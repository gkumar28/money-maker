package strategy.engine.schemaobject.signal;

import java.math.BigDecimal;

public record SignalMetaData(BigDecimal atr, BigDecimal adx, BigDecimal price) {

    public static SignalMetaData instance() {
        return new SignalMetaData(null, null, null);
    }
}
