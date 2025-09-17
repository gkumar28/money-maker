package strategy.engine.schemaobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class HoldingDto {
    private String instrument;
    private int quantity = 0;
    private BigDecimal avgEntryPrice = BigDecimal.ZERO;

    private BigDecimal currentInvestedCapital = BigDecimal.ZERO;
    private BigDecimal maxInvestedCapital = BigDecimal.ZERO;

    public HoldingDto(String instrument) {
        this.instrument = instrument;
    }
}


