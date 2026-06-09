package sre.engine.strategy.schemaobject.signal;

import lombok.With;

@With
public record SignalContext(Signal signal, SignalMetaData metaData) {

    public static SignalContext instance() {
        return new SignalContext(Signal.instance(), SignalMetaData.instance());
    }
}
