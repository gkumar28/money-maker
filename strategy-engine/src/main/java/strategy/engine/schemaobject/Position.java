package strategy.engine.schemaobject;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import strategy.engine.constant.enums.TradeType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class Position {

    private final String instrument;
    private final List<Trade> entries;
    private final List<Trade> exits;
    private final TradeType startingType;
    private BigDecimal openQuantity;

    public Position(String instrument, TradeType tradeType) {
        this.entries = new ArrayList<>();
        this.exits = new ArrayList<>();
        this.startingType = tradeType;
        this.instrument = instrument;
        this.openQuantity = BigDecimal.ZERO;
    }

    public boolean isOpen() {
        return this.openQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isClosed() {
        return !this.entries.isEmpty() && this.openQuantity.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean addEntry(Trade trade) {
        if (isClosed() || trade.getTradeType() != startingType) {
            return false;
        }

        this.entries.add(trade);
        this.openQuantity = this.openQuantity.add(trade.getQuantity());
        return true;
    }

    public boolean addExit(Trade trade) {
        if (isClosed() || this.openQuantity.compareTo(trade.getQuantity()) < 0 || trade.getTradeType() == startingType) {
            return false;
        }

        this.exits.add(trade);
        this.openQuantity = this.openQuantity.subtract(trade.getQuantity());
        return true;
    }

    public boolean operate(Trade trade) {
        if (trade.getTradeType() == startingType) {
            return addEntry(trade);
        } else {
            return addExit(trade);
        }
    }

    public BigDecimal getProfitLoss() {
        BigDecimal totalEntry = BigDecimal.ZERO;
        BigDecimal totalExit = BigDecimal.ZERO;
        BigDecimal totalEntryCost = BigDecimal.ZERO;
        BigDecimal totalExitCost = BigDecimal.ZERO;

        for (Trade trade : entries) {
            BigDecimal amount = trade.getGrossValue();
            if (trade.getTradeType() == TradeType.BUY) {
                totalEntry = totalEntry.add(amount);
            } else {
                totalEntry = totalEntry.subtract(amount); // for short entry
            }

            totalEntryCost = totalEntryCost.add(trade.getCost());
        }

        for (Trade trade : exits) {
            BigDecimal amount = trade.getGrossValue();
            if (trade.getTradeType() == TradeType.SELL) {
                totalExit = totalExit.add(amount);
            } else {
                totalExit = totalExit.subtract(amount); // for short exit
            }

            totalExitCost = totalExitCost.add(trade.getCost());
        }

        return totalExit.subtract(totalEntry).subtract(totalEntryCost).subtract(totalExitCost);
    }

    public BigDecimal getInvestedCapital() {

        BigDecimal remainingExitQty = BigDecimal.ZERO;
        BigDecimal capital = BigDecimal.ZERO;

        for (Trade exit : exits) {
            remainingExitQty = remainingExitQty.add(exit.getQuantity());
        }

        for (Trade entry : entries) {
            BigDecimal entryQty = entry.getQuantity();
            BigDecimal matchQty = entryQty.min(remainingExitQty);
            BigDecimal unmatchedQty = entryQty.subtract(matchQty);

            capital = capital.add(unmatchedQty.multiply(entry.getPrice())).add(entry.getCost());
            remainingExitQty = remainingExitQty.subtract(matchQty);
        }

        return capital;
    }
}
