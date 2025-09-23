package strategy.engine.schemaobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
public class Holding {
    private String instrument;
    private BigDecimal quantity = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    private BigDecimal avgEntryPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private BigDecimal lastTradePrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private BigDecimal currentInvestedCapital = BigDecimal.ZERO;
    private BigDecimal maxInvestedCapital = BigDecimal.ZERO;

    public Holding(String instrument) {
        this.instrument = instrument;
    }
}


