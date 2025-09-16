package strategy.engine.schemaobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import strategy.engine.constant.enums.TradeDirection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDto {
    private String instrument;
    private TradeDirection direction;
    private int quantity;
    private BigDecimal price;
    private int index;
    private ZonedDateTime timestamp;
}

