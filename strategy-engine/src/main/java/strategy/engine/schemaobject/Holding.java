package strategy.engine.schemaobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Holding {
    private String instrument;
    private int quantity = 0;
    private BigDecimal avgEntryPrice = BigDecimal.ZERO;
    private BigDecimal lastTradePrice = BigDecimal.ZERO;

    private BigDecimal currentInvestedCapital = BigDecimal.ZERO;
    private BigDecimal maxInvestedCapital = BigDecimal.ZERO;

    public Holding(String instrument) {
        this.instrument = instrument;
    }
}


