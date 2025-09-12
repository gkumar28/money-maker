package strategy.engine.schemaobject;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@NoArgsConstructor
@Data
public class BarDataDto {

    @NotNull
    private Long duration;
    @NotNull
    private ZonedDateTime endTime;
    @NotNull
    private BigDecimal open;
    @NotNull
    private BigDecimal high;
    @NotNull
    private BigDecimal low;
    @NotNull
    private BigDecimal close;
    @NotNull
    private BigDecimal volume;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Long trades;
}
