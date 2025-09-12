package strategy.engine.schemaobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import strategy.engine.constant.enums.TradeDirection;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyOrderDto {

    private String instrument;
    private ZonedDateTime timestamp;
    private TradeDirection direction;
    private int quantity;
    private BigDecimal price;
    private BigDecimal slPrice;
    private BigDecimal tpPrice;
    private BigDecimal signalStrength;
    private BigDecimal capitalAllocated;

    public static StrategyOrderDto empty(String instrument, SignalDto signal) {
        StrategyOrderDto order = new StrategyOrderDto();
        order.setInstrument(instrument);
        order.setTimestamp(signal.getTimestamp());
        order.setDirection(null);
        order.setQuantity(0);
        order.setPrice(null);
        order.setSlPrice(null);
        order.setTpPrice(null);
        order.setSignalStrength(signal.getConfidence());
        order.setCapitalAllocated(BigDecimal.ZERO);
        return order;
    }

}
