package strategy.engine.schemaobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class HoldingDto {
    private String instrument;
    private int quantity;
    private BigDecimal avgEntryPrice;
}


