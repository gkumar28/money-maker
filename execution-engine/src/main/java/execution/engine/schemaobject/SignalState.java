package execution.engine.schemaobject;

import execution.engine.constant.enums.TradeSignal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalState {
    private TradeSignal signal;
    private ZonedDateTime timestamp;
    private BigDecimal price;
}