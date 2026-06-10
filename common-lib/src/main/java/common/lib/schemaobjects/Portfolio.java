package common.lib.schemaobjects;

import lombok.*;
import org.ta4j.core.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Portfolio {
    private BigDecimal initialCapital;
    private BigDecimal availableCapital;
    private BigDecimal investedCapital;
    private BigDecimal maxInvestedCapital;
    private BigDecimal realizedPnL = BigDecimal.ZERO;
    private Instant lastUpdated;
    private BigDecimal value;
    private Map<String, Holding> holdings = new HashMap<>();
    private Map<String, BigDecimal> slPrices = new HashMap<>();
    private Map<String, BigDecimal> tpPrices = new HashMap<>();

    private volatile Portfolio.Snapshot snapshot;
    private volatile int version = 0;
    private volatile int snapshotVersion = -1;
    private final Object lock = new Object();

    public void removeHolding(String instrument) {
        putHolding(instrument, null);
    }
    public void putHolding(String instrument, Holding value) {

        synchronized (lock) {
            version++;
            updateMapInternal(getHoldings(), instrument, value);
        }
    }

    public void removeSlPrice(String instrument) { putSlPrice(instrument, null); }
    public void putSlPrice(String instrument, BigDecimal value) {

        synchronized (lock) {
            version++;
            updateMapInternal(getSlPrices(), instrument, value);
        }
    }

    public void removeTpPrice(String instrument) { putTpPrice(instrument, null); }
    public void putTpPrice(String instrument, BigDecimal value) {

        synchronized (lock) {
            version++;
            updateMapInternal(getTpPrices(), instrument, value);
        }
    }

    public void reset(BigDecimal newCapital) {
        synchronized (lock) {
            version++;
            this.getHoldings().clear();
            this.setAvailableCapital(newCapital);
            this.setRealizedPnL(BigDecimal.ZERO);
            this.setInitialCapital(newCapital);
            this.setInvestedCapital(BigDecimal.ZERO);
            this.setMaxInvestedCapital(BigDecimal.ZERO);
            this.getTpPrices().clear();
            this.getSlPrices().clear();
        }
    }

    private <V> void updateMapInternal(Map<String, V> map, String key, V value) {
        if (null == value) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    public void applyTrade(Trade trade) {
        synchronized (lock) {
            String instrument = trade.getInstrument();
            Holding holding = this.getHoldings().getOrDefault(instrument, Holding.instance(instrument));
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

            setAvailableCapital(getAvailableCapital().subtract(capitalUtilizedInTrade));
            setInvestedCapital(getInvestedCapital().add(capitalUtilizedInTrade));
            setMaxInvestedCapital(getMaxInvestedCapital().max(getInvestedCapital()));
            setValue(getValue().add(newValue).subtract(holding.value()));

            if (newQty.compareTo(BigDecimal.ZERO) > 0) { updateMapInternal(getHoldings(), instrument, newHolding); }
            else { updateMapInternal(getHoldings(), instrument, null); }

            version++;
        }
    }

    public Snapshot snapshot() {
        if (snapshotVersion != version) {
            synchronized (lock) {
                if (snapshotVersion != version) {

                    this.snapshot = new Snapshot(
                            initialCapital,
                            availableCapital,
                            investedCapital,
                            maxInvestedCapital,
                            value,
                            Map.copyOf(holdings),
                            Map.copyOf(slPrices),
                            Map.copyOf(tpPrices));
                    snapshotVersion = version;
                }
            }
        }
        return snapshot;
    }

    public record Snapshot(
            BigDecimal initialCapital,
            BigDecimal availableCapital,
            BigDecimal investedCapital,
            BigDecimal maxInvestedCapital,
            BigDecimal value,
            Map<String, Holding> holdings,
            Map<String, BigDecimal> slPrices,
            Map<String, BigDecimal> tpPrices
    ) {
        public Holding getHolding(String instrument) {
            return holdings().getOrDefault(instrument, Holding.instance(instrument));
        }

        public BigDecimal getSlPrice(String instrument) {
            return slPrices().getOrDefault(instrument, BigDecimal.ZERO);
        }

        public BigDecimal getTpPrice(String instrument) {
            return tpPrices().getOrDefault(instrument, BigDecimal.valueOf(1e9));
        }
    }
}

