package strategy.engine.schemaobject;

import strategy.engine.constant.enums.TradeAction;
import strategy.engine.constant.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Signal {

    public Signal(TradeType tradeType, ZonedDateTime timestamp) {
        this.tradeType = tradeType;
        this.timestamp = timestamp;
    }

    private TradeType tradeType;
    private TradeAction action;
    private ZonedDateTime timestamp;
    private BigDecimal price;

    // indicators
    private BigDecimal atr;
    private BigDecimal adx;
}