package strategy.engine.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.reports.TradingStatementGenerator;
import strategy.engine.component.TradingStrategyFactory;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.indicator.KallmanIndicator;
import strategy.engine.schemaobject.HoldingDto;
import strategy.engine.schemaobject.PortfolioDto;
import strategy.engine.schemaobject.SignalDto;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.schemaobject.TradeDto;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.schemaobject.TradingReportGenerator;
import strategy.engine.schemaobject.analysis.ExtendedTradeExecutionModel;
import strategy.engine.schemaobject.analysis.MultiPositionTradeOnNextOpenModel;
import strategy.engine.schemaobject.analysis.MultiPositionTradingRecord;
import strategy.engine.service.BacktestService;
import strategy.engine.service.MarketDataService;
import strategy.engine.service.PortfolioService;
import strategy.engine.service.PositionManagementService;
import strategy.engine.strategy.TradingStrategy;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static strategy.engine.util.StrategyEngineUtils.asTradeType;

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
        for (String instrument: instruments) {
            TradingRecord tradingRecord = backtestPerInstrument(instrument, exchange, interval, from, to, strategyType);
            if (null != tradingRecord) {
                tradingReportGenerator.setTradingRecord(instrument, tradingRecord);
            }
        }

        return tradingReportGenerator.generate();
    }

    private TradingRecord backtestPerInstrument(String instrument, String exchange, String interval, LocalDateTime from, LocalDateTime to, StrategyType strategyType) {
        Path dataFilePath = marketDataService.loadRawData(instrument, exchange, from, to, interval);
        try(Stream<String> lines = Files.lines(dataFilePath)) {

            BarSeries barSeries = new BaseBarSeries(instrument);
            barSeries.setMaximumBarCount(100);
            TradingStrategy strategy = tradingStrategyFactory.create(strategyType, barSeries);
            TradingRecord tradingRecord = new MultiPositionTradingRecord(instrument, Trade.TradeType.BUY);
            ExtendedTradeExecutionModel tradeExecutionModel = new MultiPositionTradeOnNextOpenModel();

            Iterator<String> fileIterator = lines.skip(1).iterator();
            int index = 0;
            while(fileIterator.hasNext()) {
                Bar bar = marketDataService.historicalCsvStringToBar(fileIterator.next(), getDuration(interval));
                barSeries.addBar(bar);

                if (index > 0) {
                    SignalDto newSignal = strategy.evaluate(index - 1);
                    StrategyOrderDto strategyOrderDto = positionManagementService.createOrderForLongPosition(instrument, newSignal);

                    if (strategyOrderDto.getQuantity() > 0) {
                        TradeDto executedOrder = tradeExecutionModel.execute(index - 1, tradingRecord, barSeries, DecimalNum.valueOf(strategyOrderDto.getQuantity()), asTradeType(strategyOrderDto.getDirection()));
                        executedOrder.setTimestamp(bar.getEndTime().minus(bar.getTimePeriod()));
                        portfolioService.applyOrder(executedOrder);
                    }
                }

                index++;
            }

            return tradingRecord;
        } catch (Exception exception) {
            log.error("Backtest failed", exception);
        }
        return null;
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
