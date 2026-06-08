package strategy.engine.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.TradeExecutionModel;
import org.ta4j.core.backtest.TradeOnNextOpenModel;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNumFactory;
import strategy.engine.component.Registry;
import strategy.engine.indicator.KallmanIndicator;
import strategy.engine.schemaobject.Portfolio;
import strategy.engine.schemaobject.barseries.LookAheadBarSeries;
import strategy.engine.schemaobject.signal.Signal;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.schemaobject.TradingReportGenerator;
import strategy.engine.schemaobject.barseries.LookAheadBarSeriesBuilder;
import strategy.engine.service.BacktestService;
import strategy.engine.service.MarketDataService;
import strategy.engine.service.PortfolioManagementService;
import strategy.engine.service.RiskManagementService;
import strategy.engine.service.TradingRecordManagementService;
import strategy.engine.strategy.SignalStrategy;
import strategy.engine.strategy.StrategyDefinition;
import strategy.engine.strategy.StrategyDefinitionParser;
import strategy.engine.strategy.DiscreteSignalStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class BacktestServiceImpl implements BacktestService {


    private final MarketDataService marketDataService;
    private final PortfolioManagementService portfolioManagementService;
    private final RiskManagementService riskManagementService;
    private final StrategyDefinitionParser strategyDefinitionParser;
    private final TradingRecordManagementService tradingRecordManagementService;

    @Override
    public TradingReport backtest(List<String> instruments, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        StrategyDefinition strategyDefinition = strategyDefinitionParser.readAny();
        return backtest(instruments, exchange, interval, fromDate, toDate, true, strategyDefinition);
    }

    @Override
    public TradingReport backtest(List<String> instruments, String exchange, String interval, LocalDate fromDate, LocalDate toDate, boolean writeResultToFile, StrategyDefinition strategyDefinition) {
        LocalDateTime from = fromDate.atTime(LocalTime.of(0, 0, 0));
        LocalDateTime to = toDate.atTime(LocalTime.of(23, 59, 59));

        // setup backtest
        Portfolio portfolio = new Portfolio();
        portfolio.reset(BigDecimal.valueOf(1000000));
        TradingReportGenerator tradingReportGenerator = new TradingReportGenerator(portfolio);
        List<String> readyToTest = new ArrayList<>();
        Registry<TradingRecord> tradingRecordRegistry = new Registry<>();
        Registry<BarSeries> barSeriesRegistry = new Registry<>();
        TradeExecutionModel executionModel = new TradeOnNextOpenModel();
        if (null != interval) {
            strategyDefinition = strategyDefinition.withInterval(interval);
        }

        for (String instrument: instruments) {
            Path dataFilePath = marketDataService.loadRawData(instrument, exchange, from, to, interval);
            BarSeries barSeries = getBacktestBarSeries(instrument, dataFilePath);
            boolean instrumentBacktestLoaded = initBacktestForInstrument(instrument, dataFilePath, strategyDefinition);
            if (instrumentBacktestLoaded) {
                readyToTest.add(instrument);
                tradingReportGenerator.setTradingRecord(instrument, tradingRecordRegistry.get(instrument));
            }
        }

        boolean hasNext = true;
        int index = 0;
        while (hasNext) {
            hasNext = false;
            for (String instrument: readyToTest) {
                boolean currentHasNext = backtestPerInstrument(index,
                    instrument,
                    portfolio,
                    fileIterators.get(instrument),
                    strategyRegistry.get(instrument),
                        executionModel);
                hasNext = hasNext || currentHasNext;
            }
            index++;
        }

        if (writeResultToFile) {
            for (String instrument : readyToTest) {
                tradingRecordManagementService.writeToFile(strategies.get(instrument).getTradingRecord(), instrument, exchange, from, to, interval);
            }
        }
        TradingReport tradingReport = tradingReportGenerator.generate();

        // clear backtest
        portfolioManagementService.resetPortfolio(portfolio, BigDecimal.ZERO);
        log.debug("\n================================ END OF BACK TEST =================================\n");
        return tradingReport;
    }

    private BarSeries getBacktestBarSeries(String instrument, Path dataFilePath) {

        try {
            Stream<String> lines = Files.lines(dataFilePath);
            Iterator<String> fileIterator = lines.skip(1).iterator();

            return new LookAheadBarSeriesBuilder()
                    .withName(instrument)
                    .withNumFactory(DecimalNumFactory.getInstance())
                    .withMaxBarCount(2000)
                    .withSeriesBeginIndex(0)
                    .withDataProvider(new LookAheadBarSeries.BarDataProvider() {

                        Path path = dataFilePath;

                        @Override
                        public List<Bar> fetchBars(int startIndex, int count) {
                            return List.of();
                        }
                    })
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize instrument {}", instrument, e);
        }

        return new BaseBarSeriesBuilder().build();
    }

    private boolean backtestPerInstrument(int index,
                                          String instrument,
                                          Portfolio portfolio,
                                          Iterator<String> fileIterator,
                                          DiscreteSignalStrategy DiscreteSignalStrategy,
                                          TradeExecutionModel tradeExecutionModel){
        if (!fileIterator.hasNext()) {
            return false;
        }

        BarSeries barSeries = DiscreteSignalStrategy.getBarSeries();
        TradingRecord tradingRecord = DiscreteSignalStrategy.getTradingRecord();

        Bar bar = marketDataService.historicalCsvStringToBar(fileIterator.next(), getDuration(DiscreteSignalStrategy.getInterval()), DecimalNumFactory.getInstance());
        DiscreteSignalStrategy.getBarSeries().addBar(bar);

        if (index > 0) {
            Signal newSignal = DiscreteSignalStrategy.evaluate(index - 1);
            Order order = riskManagementService.triggerSLTP(portfolio, instrument, newSignal, barSeries.getBar(index - 1).getClosePrice().bigDecimalValue());
            if (null == order) {
                order = riskManagementService.createOrder(portfolio, instrument, newSignal);
            }

            if (null != order.getTradeType()) {
                if (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    Trade executedTrade = tradeExecutionModel.execute(index - 1, tradingRecord, barSeries, order);

                    portfolioManagementService.applyTrade(portfolio, executedTrade);
                    riskManagementService.updateSLTP(portfolio, instrument);
                } else {
                    log.debug("{}: index: {} No trade as quantity too low", instrument, index - 1);
                }
            }
        }
        return fileIterator.hasNext();
    }

    private Duration getDuration(String interval) {
        return switch(interval) {
            case "minute", "1minute" -> Duration.ofMinutes(1);
            case "3minute" -> Duration.ofMinutes(3);
            case "5minute" -> Duration.ofMinutes(5);
            case "60minute" -> Duration.ofMinutes(60);
            default -> Duration.ofDays(1);
        };
    }

    @Override
    public List<Map<String, Object>> getIndicatorValues(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate.atTime(LocalTime.of(0, 0, 0));
        LocalDateTime to = toDate.atTime(LocalTime.of(23, 59, 59));
        Path dataFilePath = marketDataService.loadRawData(instrument, exchange, from, to, interval);
        List<Map<String, Object>> result = new ArrayList<>();

        try(Stream<String> lines = Files.lines(dataFilePath)) {

            BarSeries barSeries = new BaseBarSeriesBuilder()
                    .withName(instrument + ":backtest")
                    .withMaxBarCount(100)
                    .withNumFactory(DecimalNumFactory.getInstance())
                    .build();

            ClosePriceIndicator close = new ClosePriceIndicator(barSeries);
            KallmanIndicator kallman = new KallmanIndicator(close, 0.1, 5);

            Iterator<String> fileIterator = lines.skip(1).iterator();
            int index = 0;
            while(fileIterator.hasNext()) {
                Map<String, Object> data = new HashMap<>();
                data.put("closePrice", close.getValue(index).bigDecimalValue());
                data.put("kallmanPrice", kallman.getValue(index).bigDecimalValue());
                data.put("% value", close.getValue(index).bigDecimalValue().divide(kallman.getValue(index).bigDecimalValue(), 2, RoundingMode.HALF_UP));

                result.add(data);
                index++;
            }
        } catch (Exception exception) {
            log.error("Backtest failed", exception);
        }

        return result;
    }


}
