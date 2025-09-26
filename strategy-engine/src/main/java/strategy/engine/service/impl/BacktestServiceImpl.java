package strategy.engine.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.constant.enums.TradeType;
import strategy.engine.indicator.KallmanIndicator;
import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.Trade;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.schemaobject.TradingReportGenerator;
import strategy.engine.schemaobject.analysis.MultiLegPositionTradingRecord;
import strategy.engine.schemaobject.analysis.TradeExecutionModel;
import strategy.engine.schemaobject.analysis.TradeOnNextOpenModel;
import strategy.engine.schemaobject.analysis.TradingRecord;
import strategy.engine.schemaobject.analysis.ZeroCost;
import strategy.engine.service.BacktestService;
import strategy.engine.service.MarketDataService;
import strategy.engine.service.PortfolioService;
import strategy.engine.service.PositionManagementService;
import strategy.engine.service.TradingRecordManagementService;
import strategy.engine.strategy.StrategyDefinition;
import strategy.engine.strategy.StrategyDefinitionParser;
import strategy.engine.strategy.StrategyInstance;
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
    private final PortfolioService portfolioService;
    private final PositionManagementService positionManagementService;
    private final StrategyDefinitionParser strategyDefinitionParser;
    private final TradingRecordManagementService tradingRecordManagementService;

    @Override
    public TradingReport backtest(List<String> instruments, String exchange, String interval, StrategyType strategyType, LocalDate fromDate, LocalDate toDate) {

        LocalDateTime from = fromDate.atTime(LocalTime.of(0, 0, 0));
        LocalDateTime to = toDate.atTime(LocalTime.of(23, 59, 59));
        TradingReportGenerator tradingReportGenerator = new TradingReportGenerator(portfolioService.getPortfolio());

        // setup backtest
        portfolioService.resetPortfolio(BigDecimal.valueOf(1000000));
        List<String> readyToTest = new ArrayList<>();
        Map<String, Iterator<String>> fileIterators = new HashMap<>();
        Map<String, StrategyInstance> strategies = new HashMap<>();
        TradeExecutionModel tradeExecutionModel = new TradeOnNextOpenModel(new ZeroCost());
        StrategyDefinition strategyDefinition = strategyDefinitionParser.readAny();
        if (null != interval) {
            strategyDefinition = strategyDefinition.withInterval(interval);
        }

        for (String instrument: instruments) {
            Path dataFilePath = marketDataService.loadRawData(instrument, exchange, from, to, interval);
            boolean instrumentBacktestLoaded = initBacktestForInstrument(instrument, dataFilePath, fileIterators, strategyDefinition, strategies);
            if (instrumentBacktestLoaded) {
                readyToTest.add(instrument);
                tradingReportGenerator.setTradingRecord(instrument, strategies.get(instrument).getTradingRecord());
            }
        }

        boolean hasNext = true;
        int index = 0;
        while (hasNext) {
            hasNext = false;
            for (String instrument: readyToTest) {
                boolean currentHasNext = backtestPerInstrument(index,
                    instrument,
                    fileIterators.get(instrument),
                    strategies.get(instrument),
                    tradeExecutionModel);
                hasNext = hasNext || currentHasNext;
            }
            index++;
        }

        for (String instrument: readyToTest) {
            tradingRecordManagementService.writeToFile(strategies.get(instrument).getTradingRecord(), instrument, exchange, from, to, interval);
        }
        TradingReport tradingReport = tradingReportGenerator.generate();

        // clear backtest
        portfolioService.resetPortfolio(BigDecimal.ZERO);
        log.debug("\n================================ END OF BACK TEST =================================\n");
        return tradingReport;
    }

    private boolean initBacktestForInstrument(String instrument,
                              Path dataFilePath,
                              Map<String, Iterator<String>> fileIterators,
                              StrategyDefinition strategyDefinition,
                              Map<String, StrategyInstance> strategies) {

        try {
            Stream<String> lines = Files.lines(dataFilePath);
            Iterator<String> fileIterator = lines.skip(1).iterator();
            fileIterators.put(instrument, fileIterator);

            BarSeries series = new BaseBarSeries(instrument);
            series.setMaximumBarCount(5000);

            TradingRecord tradingRecord = new MultiLegPositionTradingRecord(instrument, TradeType.BUY);
            StrategyInstance strategyInstance = new StrategyInstance(instrument, strategyDefinition, tradingRecord, series);
            strategies.put(instrument, strategyInstance);
        } catch (Exception e) {
            log.error("Failed to initialize instrument {}", instrument, e);
            return false;
        }

        return true;
    }

    private boolean backtestPerInstrument(int index,
                                          String instrument,
                                          Iterator<String> fileIterator,
                                          StrategyInstance strategyInstance,
                                          TradeExecutionModel tradeExecutionModel){
        if (!fileIterator.hasNext()) {
            return false;
        }

        BarSeries barSeries = strategyInstance.getBarSeries();
        TradingRecord tradingRecord = strategyInstance.getTradingRecord();

        Bar bar = marketDataService.historicalCsvStringToBar(fileIterator.next(), getDuration(strategyInstance.getInterval()));
        strategyInstance.getBarSeries().addBar(bar);

        if (index > 0) {
            portfolioService.updateLastTradedPrice(instrument, barSeries.getBar(index - 1).getClosePrice().bigDecimalValue());
            Signal newSignal = strategyInstance.evaluate(index - 1);
            Order order = positionManagementService.triggerSLTPForPosition(instrument, newSignal, barSeries.getBar(index - 1).getClosePrice().bigDecimalValue());
            if (null == order) {
                order = positionManagementService.createOrderForLongPosition(instrument, newSignal);
            }

            if (null != order.getTradeType()) {
                if (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    Trade executedTrade = tradeExecutionModel.execute(index - 1, tradingRecord, barSeries, order);

                    portfolioService.applyTrade(executedTrade, tradingRecord);
                    positionManagementService.updateSlTpForInstrument(instrument);
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

            BarSeries barSeries = new BaseBarSeries(instrument + ":backtest");
            barSeries.setMaximumBarCount(100);
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
