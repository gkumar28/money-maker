package strategy.engine.schemaobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TradingResultDto {

    // position stats
    private int profitCount;
    private int breakEvenCount;
    private int lossCount;

    // performance
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercentage;
    private BigDecimal totalProfit;
    private BigDecimal totalLoss;
}
