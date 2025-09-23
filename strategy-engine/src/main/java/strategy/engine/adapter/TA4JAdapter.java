package strategy.engine.adapter;

import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DecimalNum;
import strategy.engine.constant.enums.TradeType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TA4JAdapter {

    private TA4JAdapter() {}

    public static Position asTA4JPosition(strategy.engine.schemaobject.Position position) {

        if (!position.isOpen() && !position.isClosed()) {
            return new Position(getTA4JTradeType(position.getStartingType()));
        }
        BigDecimal totalQuantity = BigDecimal.ZERO;

        BigDecimal entryCapital = BigDecimal.ZERO;
        int earliestEntryIndex = Integer.MAX_VALUE;
        for (strategy.engine.schemaobject.Trade trade: position.getEntries()) {
            entryCapital = entryCapital.add(trade.getGrossValue()).subtract(trade.getCost());
            totalQuantity = totalQuantity.add(trade.getQuantity());
            earliestEntryIndex = Math.min(earliestEntryIndex, trade.getIndex());
        }
        BigDecimal avgEntryPrice = entryCapital.divide(totalQuantity, RoundingMode.HALF_UP);

        BigDecimal exitCapital = BigDecimal.ZERO;
        int latestExitIndex = Integer.MIN_VALUE;
        for (strategy.engine.schemaobject.Trade trade: position.getEntries()) {
            exitCapital = exitCapital.add(trade.getGrossValue()).subtract(trade.getCost());
            latestExitIndex = Math.max(latestExitIndex, trade.getIndex());
        }
        BigDecimal avgExitPrice = exitCapital.divide(totalQuantity, RoundingMode.HALF_UP);

        if (position.isOpen()) {
            Trade entry = asTrade(earliestEntryIndex, position.getStartingType(), position.getInvestedCapital().divide(position.getOpenQuantity(), RoundingMode.HALF_UP), position.getOpenQuantity());
            Position result = new Position(getTA4JTradeType(position.getStartingType()));
            result.operate(entry.getIndex(), entry.getPricePerAsset(), entry.getAmount());
            return result;
        }

        Trade entry = asTrade(earliestEntryIndex, position.getStartingType(), avgEntryPrice, totalQuantity);
        Trade exit = asTrade(earliestEntryIndex, position.getStartingType().complementType(), avgExitPrice, totalQuantity);
        return new Position(entry, exit);
    }

    public static TradingRecord asTradingRecord(strategy.engine.schemaobject.analysis.TradingRecord tradingRecord) {
        Position position = asTA4JPosition(tradingRecord.getOpenPosition());
        if (position.isNew()) {
            return new BaseTradingRecord();
        }

        if (position.isOpened()) {
            return new BaseTradingRecord(position.getEntry());
        }

        return new BaseTradingRecord(position.getEntry(), position.getExit());
    }

    private static Trade.TradeType getTA4JTradeType(TradeType tradeType) {
        if (TradeType.BUY == tradeType) {
            return Trade.TradeType.BUY;
        }
        return Trade.TradeType.SELL;
    }

    private static Trade asTrade(int index, TradeType tradeType, BigDecimal price, BigDecimal quantity) {
        if (getTA4JTradeType(tradeType) == Trade.TradeType.BUY) {
            return Trade.buyAt(index, DecimalNum.valueOf(price), DecimalNum.valueOf(quantity));
        }

        return Trade.sellAt(index, DecimalNum.valueOf(price), DecimalNum.valueOf(quantity));
    }
}
