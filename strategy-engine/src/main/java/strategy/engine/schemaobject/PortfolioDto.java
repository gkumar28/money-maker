package strategy.engine.schemaobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Data
public class PortfolioDto {
    private BigDecimal totalCapital;
    private BigDecimal availableCapital;
    private BigDecimal currentInvestedCapital;
    private BigDecimal maxInvestedCapital;
    private BigDecimal realizedPnL = BigDecimal.ZERO;
    private Map<String, HoldingDto> holdings = new HashMap<>();
    private List<TradeDto> tradeDtoList = new ArrayList<>();
    private ZonedDateTime lastUpdated;

    public void setLastUpdated() {
        this.lastUpdated = ZonedDateTime.now(ZoneId.of("UTC"));
    }
}

