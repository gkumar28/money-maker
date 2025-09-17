package strategy.engine.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import strategy.engine.component.TradingStrategyFactory;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.indicator.KallmanIndicator;
import strategy.engine.schemaobject.SignalDto;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.schemaobject.TradeDto;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.schemaobject.TradingReportGenerator;
import strategy.engine.schemaobject.analysis.MultiPositionTradeOnNextOpenModel;
import strategy.engine.schemaobject.analysis.MultiPositionTradingRecord;
import strategy.engine.service.BacktestService;
import strategy.engine.service.MarketDataService;
import strategy.engine.service.PortfolioService;
import strategy.engine.service.PositionManagementService;
import strategy.engine.strategy.TradingStrategy;

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
    private final TradingStrategyFactory tradingStrategyFactory;

    @Override
    public TradingReport backtest(List<String> instruments, String exchange, String interval, StrategyType strategyType, LocalDate fromDate, LocalDate toDate) {
        // 1. Load historical data for the instrument
        portfolioService.resetPortfolio(BigDecimal.valueOf(1000000));
        LocalDateTime from = fromDate.atTime(LocalTime.of(0, 0, 0));
        LocalDateTime to = toDate.atTime(LocalTime.of(23, 59, 59));
        TradingReportGenerator tradingReportGenerator = new TradingReportGenerator(portfolioService.getPortfolio());

        List<String> readyToTest = new ArrayList<>();
        Map<String, Iterator<String>> fileIterators = new HashMap<>();
        Map<String, BarSeries> barSeries = new HashMap<>();
        Map<String, TradingStrategy> strategies = new HashMap<>();
        Map<String, MultiPositionTradingRecord> tradingRecords = new HashMap<>();
        MultiPositionTradeOnNextOpenModel tradeExecutionModel = new MultiPositionTradeOnNextOpenModel();

        for (String instrument: instruments) {
            Path dataFilePath = marketDataService.loadRawData(instrument, exchange, from, to, interval);
            boolean instrumentBacktestLoaded = initBacktestForInstrument(instrument, strategyType, dataFilePath, fileIterators, barSeries, strategies, tradingRecords);
            if (instrumentBacktestLoaded) {
                readyToTest.add(instrument);
                tradingReportGenerator.setTradingRecord(instrument, tradingRecords.get(instrument));
            }
        }

        boolean hasNext = true;
        int index = 0;
        while (hasNext) {
            hasNext = false;
            for (String instrument: readyToTest) {
                boolean currentHasNext = backtestPerInstrument(index,
                    instrument,
                    interval,
                    fileIterators.get(instrument),
                    barSeries.get(instrument),
                    strategies.get(instrument),
                    tradeExecutionModel,
                    tradingRecords.get(instrument));
                hasNext = hasNext || currentHasNext;
            }
            index++;
        }

        return tradingReportGenerator.generate();
    }

    private boolean initBacktestForInstrument(String instrument,
                              StrategyType strategyType,
                              Path dataFilePath,
                              Map<String, Iterator<String>> fileIterators,
                              Map<String, BarSeries> barSeries,
                              Map<String, TradingStrategy> strategies,
                              Map<String, MultiPositionTradingRecord> tradingRecords) {

        try {
            Stream<String> lines = Files.lines(dataFilePath);
            Iterator<String> fileIterator = lines.skip(1).iterator();
            fileIterators.put(instrument, fileIterator);

            BarSeries series = new BaseBarSeries(instrument);
            series.setMaximumBarCount(100);
            barSeries.put(instrument, series);

            TradingStrategy strategy = tradingStrategyFactory.create(strategyType, series);
            strategies.put(instrument, strategy);

            MultiPositionTradingRecord tradingRecord = new MultiPositionTradingRecord(instrument, Trade.TradeType.BUY);
            tradingRecords.put(instrument, tradingRecord);
        } catch (Exception e) {
            log.error("Failed to initialize instrument {}", instrument, e);
            return false;
        }

        return true;
    }

    private boolean backtestPerInstrument(int index,
                                          String instrument,
                                          String interval,
                                          Iterator<String> fileIterator,
                                          BarSeries barSeries,
                                          TradingStrategy strategy,
                                          MultiPositionTradeOnNextOpenModel tradeExecutionModel,
                                          MultiPositionTradingRecord tradingRecord) {
        Bar bar;
        try {
            bar = marketDataService.historicalCsvStringToBar(fileIterator.next(), getDuration(interval));
            barSeries.addBar(bar);
        } catch (Exception exception) {
            log.error("{}: backtesting failed at index {}", instrument, index, exception);
            return false;
        }


        if (index > 0) {
            SignalDto newSignal = strategy.evaluate(index - 1);
            StrategyOrderDto order = positionManagementService.triggerSLTPForPosition(instrument, newSignal, bar.getClosePrice().bigDecimalValue());
            if (null == order) {
                order = positionManagementService.createOrderForLongPosition(instrument, newSignal);
            }

            if (null != order.getDirection()) {
                if (order.getQuantity() > 0) {
                    TradeDto executedTrade = tradeExecutionModel.execute(index - 1, tradingRecord, barSeries, order);

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
