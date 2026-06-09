package sre.engine.strategy.schemaobject;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradingReport {

    private String instrument;
    private BigDecimal initialCapital;
    private BigDecimal availableCapital;
    private BigDecimal currentInvestedCapital = BigDecimal.ZERO; // capital invested as of end of time range
    private BigDecimal maxInvestedCapital = BigDecimal.ZERO; // maximum capital invested at a point of time
    private BigDecimal totalInvestedCapital = BigDecimal.ZERO; // capital invested over the time range
    private BigDecimal unrealizedPnL = BigDecimal.ZERO;

    // position stats
    private int profitCount;
    private int breakEvenCount;
    private int lossCount;

    // performance
    private BigDecimal profitLoss = BigDecimal.ZERO;
    private BigDecimal profitLossPercentage = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;
    private BigDecimal loss = BigDecimal.ZERO;

    private int entryTradeCount;
    private int exitTradeCount;

    List<TradingReport> subReports;
}
