package sre.engine.strategy.schemaobject.barseries;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.backtest.TradeExecutionModel;
import org.ta4j.core.backtest.TradeOnNextOpenModel;
import org.ta4j.core.num.Num;
import sre.engine.strategy.component.Registry;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class BatchedBarSeriesManager extends BarSeriesManager {

    private final TradeExecutionModel tradeExecutionModel;
    private final TradingRecordFactory tradingRecordFactory;
    private final int batchSize;
    private final Registry<Strategy> strategyRegistry;
    private final Registry<TradingRecord> tradingRecordRegistry;
    private final int seriesMaximumBarCount;
    public BatchedBarSeriesManager(int batchSize, BarSeries barSeries) {
        this(batchSize, barSeries, new ZeroCostModel(), new ZeroCostModel(), new TradeOnNextOpenModel());
    }

    public BatchedBarSeriesManager(int batchSize, BarSeries barSeries, TradeExecutionModel tradeExecutionModel) {
        this(batchSize, barSeries, new ZeroCostModel(), new ZeroCostModel(), tradeExecutionModel);
    }

    public BatchedBarSeriesManager(int batchSize, BarSeries barSeries, CostModel transactionCostModel, CostModel holdingCostModel) {
        this(batchSize, barSeries, transactionCostModel, holdingCostModel, new TradeOnNextOpenModel());
    }

    public BatchedBarSeriesManager(int batchSize, BarSeries barSeries, CostModel transactionCostModel, CostModel holdingCostModel,
                            TradeExecutionModel tradeExecutionModel) {
        super(barSeries, transactionCostModel, holdingCostModel, tradeExecutionModel);
        this.tradeExecutionModel = tradeExecutionModel;
        this.tradingRecordFactory = BaseTradingRecord::new;
        this.batchSize = batchSize;
        this.strategyRegistry = new Registry<>();
        this.tradingRecordRegistry = new Registry<>();
        this.seriesMaximumBarCount = barSeries.getMaximumBarCount();
    }


    public BatchedBarSeriesManager(int batchSize, BarSeries barSeries, CostModel transactionCostModel, CostModel holdingCostModel, TradeExecutionModel tradeExecutionModel, TradingRecordFactory tradingRecordFactory) {
        super(barSeries, transactionCostModel, holdingCostModel, tradeExecutionModel, tradingRecordFactory);
        this.tradeExecutionModel = tradeExecutionModel;
        this.tradingRecordFactory = tradingRecordFactory;
        this.batchSize = batchSize;
        this.strategyRegistry = new Registry<>();
        this.tradingRecordRegistry = new Registry<>();
        this.seriesMaximumBarCount = barSeries.getMaximumBarCount();
    }

    @Override
    public TradingRecord run(Strategy strategy) {
        return run(strategy, strategy.getStartingType());
    }

    @Override
    public TradingRecord run(Strategy strategy, int startIndex, int finishIndex) {
        return run(strategy, strategy.getStartingType(), getBarSeries().numFactory().one(), startIndex, finishIndex);
    }

    @Override
    public TradingRecord run(Strategy strategy, Trade.TradeType tradeType) {
        return run(strategy, tradeType, getBarSeries().numFactory().one());
    }

    @Override
    public TradingRecord run(Strategy strategy, Trade.TradeType tradeType, int startIndex, int finishIndex) {
        return run(strategy, tradeType, getBarSeries().numFactory().one(), startIndex, finishIndex);
    }

    @Override
    public TradingRecord run(Strategy strategy, Trade.TradeType tradeType, Num amount) {
        return run(strategy, tradeType, amount, getBarSeries().getBeginIndex(), getBarSeries().getEndIndex());
    }

    @Override
    public TradingRecord run(Strategy strategy, Trade.TradeType tradeType, Num amount, int startIndex, int finishIndex) {
        return run(strategy, createDefaultTradingRecord(tradeType, startIndex, finishIndex), amount, startIndex, finishIndex);
    }

    @Override
    public TradingRecord run(Strategy strategy, TradingRecord tradingRecord) {
        return run(strategy, tradingRecord, getBarSeries().numFactory().one());
    }

    @Override
    public TradingRecord run(Strategy strategy, TradingRecord tradingRecord, Num amount) {
        return run(strategy, tradingRecord, amount, getBarSeries().getBeginIndex(), getBarSeries().getEndIndex());
    }

    @Override
    public TradingRecord run(Strategy strategy, TradingRecord tradingRecord, Num amount, int startIndex,
                             int finishIndex) {
        if (!(getBarSeries() instanceof LookAheadBarSeries lookAheadBarSeries)) {
            return super.run(strategy, tradingRecord, amount, startIndex, finishIndex);
        }
        Objects.requireNonNull(strategy, "sre/engine");
        Objects.requireNonNull(tradingRecord, "tradingRecord");
        Objects.requireNonNull(amount, "amount");

        String id = UUID.randomUUID().toString();
        strategyRegistry.register(id, strategy);
        tradingRecordRegistry.register(id, tradingRecord);
        if (tradingRecordRegistry.size() == batchSize) { runInternal(amount, startIndex, finishIndex); }
        return tradingRecord;
    }

    private void runInternal(Num amount, int startIndex, int finishIndex) {
        int runIndex = Math.max(startIndex, getBarSeries().getBeginIndex());

        getBarSeries().getBar(startIndex);
        Set<String> ids = strategyRegistry.getAll().keySet();
        int lastProcessedIndex = runIndex;
        while(getBarSeries().getBarCount() > 0) {

            int batchBeginIndex = runIndex;
            int batchEndIndex = runIndex + getBarSeries().getBarCount() - 1;
            for(int i = batchBeginIndex; i <= batchEndIndex; i++) {
                for(String id: ids) {
                    Strategy strategy = strategyRegistry.get(id);
                    TradingRecord tradingRecord = tradingRecordRegistry.get(id);
                    tradeExecutionModel.onBar(i, tradingRecord, getBarSeries());

                    if (strategy.shouldOperate(i, tradingRecord)) {
                        tradeExecutionModel.execute(i, tradingRecord, getBarSeries(), amount);
                    }
                }
            }

            runIndex = batchEndIndex + 1;
        }

        for(TradingRecord tradingRecord: tradingRecordRegistry.getAll().values()) {
            tradeExecutionModel.onRunEnd(lastProcessedIndex, tradingRecord);
        }
    }

    private TradingRecord createDefaultTradingRecord(Trade.TradeType tradeType, int startIndex, int finishIndex) {
        int clampedStartIndex = Math.max(startIndex, getBarSeries().getBeginIndex());
        int clampedEndIndex = Math.min(finishIndex, getBarSeries().getEndIndex());
        TradingRecord tradingRecord = tradingRecordFactory.create(tradeType, clampedStartIndex, clampedEndIndex,
                getTransactionCostModel(), getHoldingCostModel());
        if (tradingRecord == null) {
            throw new IllegalStateException("tradingRecordFactory returned null");
        }
        return tradingRecord;
    }

}
