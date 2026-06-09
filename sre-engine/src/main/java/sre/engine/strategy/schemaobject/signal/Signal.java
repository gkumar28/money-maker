package sre.engine.strategy.schemaobject.signal;

import lombok.With;

import java.math.BigDecimal;
import java.time.Instant;

@With
public record Signal(String id, int index, Instant timestamp, BigDecimal exposure) {

    public static Signal instance() {
        return new Signal(null, -1, null, null);
    }

    public boolean shouldBeDiscarded() {
        return null == exposure || exposure.compareTo(BigDecimal.valueOf(1000)) == 0;
    }
}