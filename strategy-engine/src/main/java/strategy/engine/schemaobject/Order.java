package strategy.engine.schemaobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import strategy.engine.constant.enums.TradeType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String instrument;
    private ZonedDateTime timestamp;
    private TradeType tradeType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal estimatedCost;

    public static Order empty(String instrument, Signal signal) {
        Order order = new Order();
        order.setInstrument(instrument);
        order.setTimestamp(signal.getTimestamp());
        order.setTradeType(null);
        order.setQuantity(BigDecimal.ZERO);
        order.setPrice(null);
        order.setEstimatedCost(BigDecimal.ZERO);
        return order;
    }

}
