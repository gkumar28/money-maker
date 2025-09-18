package strategy.engine.schemaobject.analysis;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;

@Slf4j
public class MultiPositionTradingRecord implements ExtendedTradingRecord {

    private final String name;
    private final transient Function<Number, Num> numFunction;
    private final Trade.TradeType entryTradeType;

    private Num openQuantity;
    private final Queue<Trade> openPositions = new LinkedList<>();
    private Trade aggregatePartialExit;

    private final Integer startIndex;
    private final Integer endIndex;
    private final List<Position> closedPositions = new ArrayList<>();
    private final List<Trade> trades = new ArrayList<>();
    private final List<Trade> buyTrades = new ArrayList<>();
    private final List<Trade> sellTrades = new ArrayList<>();
    private final List<Trade> entryTrades = new ArrayList<>();
    private final List<Trade> exitTrades = new ArrayList<>();

    private final transient CostModel transactionCostModel;
    private final transient CostModel holdingCostModel;

    public MultiPositionTradingRecord(String name, Trade.TradeType entryTradeType) {
        this(name, entryTradeType, DecimalNum::valueOf);
    }

    public MultiPositionTradingRecord(String name, Trade.TradeType entryTradeType, Function<Number, Num> numFunction) {
        this(name, entryTradeType, new ZeroCostModel(), new ZeroCostModel(), numFunction);
    }

    public MultiPositionTradingRecord(String name, Trade.TradeType entryTradeType, CostModel transactionCostModel, CostModel holdingCostModel) {
        this(name, entryTradeType, transactionCostModel, holdingCostModel, DecimalNum::valueOf);
    }

    public MultiPositionTradingRecord(String name, Trade.TradeType entryTradeType, CostModel transactionCostModel, CostModel holdingCostModel, Function<Number, Num> numFunction) {
        this.name = name;
        this.entryTradeType = entryTradeType;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
        this.numFunction = numFunction;
        this.startIndex = null;
        this.endIndex = null;
        this.openQuantity = numFunction.apply(0);
        this.aggregatePartialExit = asTrade(entryTradeType.complementType(), 0, numFunction.apply(0), numFunction.apply(0));
    }

    @Override
    public Trade.TradeType getStartingType() {
        return entryTradeType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void operate(int index, Num price, Num amount) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean enter(int index, Num price, Num amount) {
        Trade entryTrade = asTrade(entryTradeType, index, price, amount);
        openQuantity = openQuantity.plus(amount);
        openPositions.add(entryTrade);
        recordTrade(entryTrade, true);
        return true;
    }

    @Override
    public boolean exit(int index, Num price, Num amount) {
        if (null == price || null == amount || price.isZero() || amount.isZero()) {
            return false;
        }
        if (openPositions.isEmpty() || openQuantity.isLessThan(amount.plus(aggregatePartialExit.getAmount()))) {
            return false;
        }

        recordTrade(asTrade(entryTradeType.complementType(), index, price, amount), false);
        boolean anyPositionClosed = false;

        while(null != openPositions.peek() && amount.isPositive()) {
            Trade earliestOpenPosition = openPositions.peek();
            if (null == earliestOpenPosition) break;
            if (aggregatePartialExit.getAmount().plus(amount).isLessThan(earliestOpenPosition.getAmount())) {
                Num newPartialExitPrice = aggregatePartialExit.getValue().plus(price.multipliedBy(amount))
                    .dividedBy(aggregatePartialExit.getAmount().plus(amount));
                aggregatePartialExit = asTrade(aggregatePartialExit.getType(), index, newPartialExitPrice, aggregatePartialExit.getAmount().plus(amount));
                break;
            }

            Num exitPricePerAsset = aggregatePartialExit.getValue().plus(price.multipliedBy(earliestOpenPosition.getAmount().minus(aggregatePartialExit.getAmount())))
                .dividedBy(earliestOpenPosition.getAmount());
            Trade mergedExit = asTrade(this.entryTradeType.complementType(), index, exitPricePerAsset, earliestOpenPosition.getAmount());
            closedPositions.add(new Position(earliestOpenPosition, mergedExit, transactionCostModel, holdingCostModel));
            openQuantity = openQuantity.minus(earliestOpenPosition.getAmount());
            openPositions.poll();

            amount = amount.plus(aggregatePartialExit.getAmount()).minus(earliestOpenPosition.getAmount());
            aggregatePartialExit = asTrade(entryTradeType.complementType(), 0, numFunction.apply(0), numFunction.apply(0));
            anyPositionClosed = true;
        }

        return anyPositionClosed;
    }

    @Override
    public CostModel getTransactionCostModel() {
        return this.transactionCostModel;
    }

    @Override
    public CostModel getHoldingCostModel() {
        return this.holdingCostModel;
    }

    @Override
    public List<Position> getPositions() {
        return closedPositions;
    }

    @Override
    public Position getCurrentPosition() {
        throw new UnsupportedOperationException("This class supports more than 1 open positions");
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
    public Trade getLastTrade(Trade.TradeType tradeType) {
        if (Trade.TradeType.BUY == tradeType && !buyTrades.isEmpty()) {
            return buyTrades.get(buyTrades.size() - 1);
        } else if (Trade.TradeType.SELL == tradeType && !sellTrades.isEmpty()) {
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
    public Num getUnRealizedCapitalInPartialPosition() {
        if (openPositions.isEmpty()) {
            return numFunction.apply(0);
        }

        Num investedCapital = openPositions.stream().map(position -> position.getAmount().multipliedBy(position.getPricePerAsset())).reduce(DecimalNum.ZERO, Num::plus);
        if (null != aggregatePartialExit) {
            Num amount = numFunction.apply(aggregatePartialExit.getAmount().bigDecimalValue());
            Iterator<Trade> openPositionIterator = openPositions.iterator();
            while(openPositionIterator.hasNext() && amount.isPositive()) {
                Trade openTrade = openPositionIterator.next();
                investedCapital = investedCapital.minus(amount.min(openTrade.getAmount()).multipliedBy(openTrade.getPricePerAsset()));
                amount = amount.minus(openTrade.getAmount()).max(numFunction.apply(0));
            }
        }

        return investedCapital;
    }

    @Override
    public List<Trade> getOpenPositions() {
        return openPositions.stream().toList();
    }

    @Override
    public Num getRealizedCapitalFromPartialPosition() {
        if (null == aggregatePartialExit || aggregatePartialExit.getAmount().isZero() || openPositions.isEmpty()) {
            return numFunction.apply(0);
        }

        Num realizedInvestedCapital = numFunction.apply(0);
        Num amount = numFunction.apply(aggregatePartialExit.getAmount().bigDecimalValue());
        Iterator<Trade> openPositionIterator = openPositions.iterator();
        while(openPositionIterator.hasNext() && amount.isPositive()) {
            Trade openTrade = openPositionIterator.next();
            realizedInvestedCapital = amount.min(openTrade.getAmount()).multipliedBy(openTrade.getPricePerAsset());
            amount = amount.minus(openTrade.getAmount()).max(numFunction.apply(0));
        }

        return realizedInvestedCapital;
    }

    @Override
    public Num getRealizedProfitLossFromPartialPosition() {
        if (null == aggregatePartialExit || aggregatePartialExit.getAmount().isZero() || openPositions.isEmpty()) {
            return numFunction.apply(0);
        }

        Num profitLoss = numFunction.apply(aggregatePartialExit.getValue().bigDecimalValue());
        Num amount = numFunction.apply(aggregatePartialExit.getAmount().bigDecimalValue());
        Iterator<Trade> openPositionIterator = openPositions.iterator();
        while(openPositionIterator.hasNext() && amount.isPositive()) {
            Trade openTrade = openPositionIterator.next();
            profitLoss = profitLoss.minus(amount.min(openTrade.getAmount()).multipliedBy(openTrade.getPricePerAsset()));
            amount = amount.minus(openTrade.getAmount()).max(numFunction.apply(0));
        }

        return profitLoss;
    }

    @Override
    public int getEntryTradeCount() {
        return entryTrades.size();
    }

    @Override
    public int getExitTradeCount() {
        return exitTrades.size();
    }

    private void recordTrade(Trade trade, boolean isEntry) {
        Objects.requireNonNull(trade, "Trade should not be null");

        if (isEntry) {
            entryTrades.add(trade);
        } else {
            exitTrades.add(trade);
        }

        // Storing the new trade in trades list
        trades.add(trade);
        if (Trade.TradeType.BUY == trade.getType()) {
            buyTrades.add(trade);
        } else if (Trade.TradeType.SELL == trade.getType()) {
            sellTrades.add(trade);
        }
    }

    private Trade asTrade(Trade.TradeType tradeType, int index, Num price, Num amount) {
        if (tradeType == Trade.TradeType.BUY) {
            return Trade.buyAt(index, price, amount);
        }
        return Trade.sellAt(index, price, amount);
    }
}
