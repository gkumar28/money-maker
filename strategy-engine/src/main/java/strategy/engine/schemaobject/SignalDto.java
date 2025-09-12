package strategy.engine.schemaobject;

import strategy.engine.constant.enums.TradeDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalDto {

    public SignalDto(TradeDirection direction, ZonedDateTime timestamp) {
        this.direction = direction;
        this.timestamp = timestamp;
    }

    private TradeDirection direction;
    private BigDecimal confidence;
    private ZonedDateTime timestamp;
    private BigDecimal price;

    // indicators
    private BigDecimal atr;
    private BigDecimal adx;
}