package strategy.engine.schemaobject;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ta4j.core.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor
@Data
public class Portfolio {
    private BigDecimal initialCapital;
    private BigDecimal availableCapital;
    private BigDecimal investedCapital;
    private BigDecimal maxInvestedCapital;
    private BigDecimal value;
    private BigDecimal realizedPnL = BigDecimal.ZERO;
    private Map<String, Holding> holdings = new ConcurrentHashMap<>();
    private Instant lastUpdated;

    // helpers
    private Map<String, BigDecimal> slPrices = new ConcurrentHashMap<>();
    private Map<String, BigDecimal> tpPrices = new ConcurrentHashMap<>();

    public Holding getHolding(String instrument) {
        return holdings.computeIfAbsent(instrument, key -> Holding.instance(instrument));
    }

    public Snapshot snapshot() {
        return new Snapshot(initialCapital, availableCapital, investedCapital, maxInvestedCapital, value, holdings);
    }

    public synchronized Snapshot applyTrade(Trade trade) {
        String instrument = trade.getInstrument();
        Holding holding = this.getHolding(instrument);
        // hack to use same logic for BUY and SELL side as they are exactly opposite in terms of holding calculation
        BigDecimal multiplier = trade.isBuy() ? BigDecimal.ONE : BigDecimal.ONE.negate();
        BigDecimal capitalUtilizedInTrade = trade.getValue().plus(trade.getCost()).bigDecimalValue().multiply(multiplier);
        BigDecimal tradeValue = trade.getValue().bigDecimalValue().multiply(multiplier);
        BigDecimal tradeQty = trade.getAmount().bigDecimalValue().multiply(multiplier);

        if (trade.isBuy() && this.getAvailableCapital().compareTo(capitalUtilizedInTrade) < 0) {
            throw new IllegalStateException("BUY trade cannot exceed available capital");
        }

        if (trade.isSell() && holding.quantity().compareTo(tradeQty.abs()) < 0) {
            throw new IllegalStateException("SELL trade cannot exceed available instrument quantity");
        }

        BigDecimal newInvestedCapital = holding.investedCapital().add(capitalUtilizedInTrade);
        BigDecimal newMaxInvestedCapital = holding.maxInvestedCapital().max(newInvestedCapital);
        BigDecimal newValue = holding.value().add(tradeValue);
        BigDecimal newQty = holding.quantity().add(tradeQty);
        BigDecimal newAvgEntryPrice = BigDecimal.ZERO.compareTo(newQty) == 0 ? BigDecimal.ZERO : newInvestedCapital.divide(newQty, 4, RoundingMode.HALF_UP);

        Holding newHolding = new Holding(
                instrument,
                newQty,
                newAvgEntryPrice,
                newInvestedCapital,
                newMaxInvestedCapital,
                newValue);

        this.getHoldings().put(instrument, newHolding);
        this.setAvailableCapital(this.getAvailableCapital().subtract(capitalUtilizedInTrade));
        this.setInvestedCapital(this.getInvestedCapital().add(capitalUtilizedInTrade));
        this.setMaxInvestedCapital(
                this.getMaxInvestedCapital().max(this.getInvestedCapital())
        );

        return this.snapshot();
    }

    public record Snapshot(
            BigDecimal initialCapital,
             BigDecimal availableCapital,
             BigDecimal investedCapital,
             BigDecimal maxInvestedCapital,
             BigDecimal value,
             Map<String, Holding> holdings
    ) {}
}

