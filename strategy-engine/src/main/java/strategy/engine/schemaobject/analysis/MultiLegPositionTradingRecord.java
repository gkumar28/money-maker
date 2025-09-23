package strategy.engine.schemaobject.analysis;

import lombok.extern.slf4j.Slf4j;
import strategy.engine.constant.enums.TradeType;
import strategy.engine.schemaobject.Position;
import strategy.engine.schemaobject.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class MultiLegPositionTradingRecord implements TradingRecord {

    private final String name;
    private final TradeType startingType;
    private Position openPosition;

    private final Integer startIndex;
    private final Integer endIndex;

    private final List<Position> closedPositions = new ArrayList<>();
    private final List<Trade> trades = new ArrayList<>();
    private final List<Trade> buyTrades = new ArrayList<>();
    private final List<Trade> sellTrades = new ArrayList<>();
    private final List<Trade> entryTrades = new ArrayList<>();
    private final List<Trade> exitTrades = new ArrayList<>();

    private final transient Cost holdingCost;

    public MultiLegPositionTradingRecord(String name, TradeType entryTradeType) {
        this(name, entryTradeType, new ZeroCost());
    }

    public MultiLegPositionTradingRecord(String name, TradeType startingType, Cost holdingCost) {
        this.name = name;
        this.startingType = startingType;
        this.startIndex = null;
        this.endIndex = null;
        this.holdingCost = holdingCost;
        this.openPosition = new Position(name, startingType);
    }

    @Override
    public TradeType getStartingType() {
        return startingType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Position getOpenPosition() { return openPosition; }

    @Override
    public List<Position> getClosedPositions() { return closedPositions; }

    @Override
    public boolean buy(int index, Trade trade) {
        return operate(index, trade);
    }

    @Override
    public boolean sell(int index, Trade trade) {
        return operate(index, trade);
    }

    @Override
    public boolean operate(int index, Trade trade) {
        if (null == openPosition) {
            openPosition = new Position(name, startingType);
        }

        boolean isRecorded = openPosition.operate(trade);
        recordTrade(trade, trade.getTradeType() == startingType);
        if (openPosition.isClosed()) {
            closedPositions.add(openPosition);
            openPosition = new Position(name, startingType);
        }

        return isRecorded;
    }

    @Override
    public List<Trade> getTrades() {
        return trades;
    }

    @Override
    public Trade getLastTrade() {
        if (!trades.isEmpty()) {
            return trades.get(trades.size() - 1);
        }
        return null;
    }

    @Override
    public Trade getLastTrade(TradeType tradeType) {
        if (TradeType.BUY == tradeType && !buyTrades.isEmpty()) {
            return buyTrades.get(buyTrades.size() - 1);
        } else if (TradeType.SELL == tradeType && !sellTrades.isEmpty()) {
            return sellTrades.get(sellTrades.size() - 1);
        }
        return null;
    }

    @Override
    public Trade getLastEntry() {
        if (!entryTrades.isEmpty()) {
            return entryTrades.get(entryTrades.size() - 1);
        }
        return null;
    }

    @Override
    public Trade getLastExit() {
        if (!exitTrades.isEmpty()) {
            return exitTrades.get(exitTrades.size() - 1);
        }
        return null;
    }

    @Override
    public Integer getStartIndex() {
        return this.startIndex;
    }

    @Override
    public Integer getEndIndex() {
        return this.endIndex;
    }

    @Override
    public int getEntryTradeCount() {
        return entryTrades.size();
    }

    @Override
    public int getExitTradeCount() {
        return exitTrades.size();
    }

    @Override
    public int getBuyTradeCount() { return buyTrades.size(); }

    @Override
    public int getSellTradeCount() { return sellTrades.size(); }

    @Override
    public int getTradeCount() { return trades.size(); }

    private void recordTrade(Trade trade, boolean isEntry) {
        Objects.requireNonNull(trade, "Trade should not be null");

        if (isEntry) {
            entryTrades.add(trade);
        } else {
            exitTrades.add(trade);
        }

        // Storing the new trade in trades list
        trades.add(trade);
        if (TradeType.BUY == trade.getTradeType()) {
            buyTrades.add(trade);
        } else if (TradeType.SELL == trade.getTradeType()) {
            sellTrades.add(trade);
        }
    }
}
