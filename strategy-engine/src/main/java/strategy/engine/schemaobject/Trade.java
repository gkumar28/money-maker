package strategy.engine.schemaobject;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import strategy.engine.constant.enums.TradeType;
import strategy.engine.schemaobject.analysis.Cost;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {
    private String instrument;
    private TradeType tradeType;
    private BigDecimal quantity;
    private BigDecimal price;
    private int index;
    private ZonedDateTime timestamp;
    private Cost transactionCost;

    public BigDecimal getGrossValue() {
        return this.price.multiply(quantity);
    }

    public BigDecimal getCost() {
        return transactionCost.calculate(price, quantity);
    }
}

