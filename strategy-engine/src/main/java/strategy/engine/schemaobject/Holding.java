package strategy.engine.schemaobject;

import lombok.With;

import java.math.BigDecimal;

@With
public record Holding(String instrument, BigDecimal quantity, BigDecimal avgEntryPrice, BigDecimal investedCapital, BigDecimal maxInvestedCapital, BigDecimal value) {

    public static Holding instance(String instrument) {
        return new Holding(
                instrument,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
                );
    }
}


