package strategy.engine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.reports.TradingStatement;
import strategy.engine.component.TradingStrategyFactory;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.indicator.KallmanIndicator;
import strategy.engine.schemaobject.BarDataDto;
import strategy.engine.schemaobject.SignalDto;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.schemaobject.TradingResultDto;
import strategy.engine.service.BacktestService;
import strategy.engine.service.MarketDataService;
import strategy.engine.service.PositionManagementService;
import strategy.engine.strategy.TradingStrategy;

import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Profile("dev")
public class TestingApiController implements TestingApi {

    private final TradingStrategyFactory tradingStrategyFactory;
    private final PositionManagementService positionManagementService;
    private final BacktestService backtestService;
    private final MarketDataService marketDataService;

    @Override
    public ResponseEntity<List<SignalDto>> simulateSignals(String instrument, StrategyType type, List<BarDataDto> input) {

        List<SignalDto> result = new ArrayList<>();
        BarSeries barSeries = new BaseBarSeries(instrument.toUpperCase() + ":" + type.toString());
        barSeries.setMaximumBarCount(100);
        TradingStrategy strategy = tradingStrategyFactory.create(type, barSeries);
        for (BarDataDto barDataDto : input) {
            barSeries.addBar(new BaseBar(
                Duration.of(barDataDto.getDuration(), ChronoUnit.SECONDS),
                barDataDto.getEndTime(),
                barDataDto.getOpen(),
                barDataDto.getHigh(),
                barDataDto.getLow(),
                barDataDto.getClose(),
                barDataDto.getVolume(),
                barDataDto.getAmount(),
                barDataDto.getTrades(),
                DecimalNum::valueOf));

        }

        for (int i=0;i<input.size();i++) {
            SignalDto signal = strategy.evaluate(i);
            result.add(signal);
        }

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<StrategyOrderDto>> simulateOrders(String instrument, List<SignalDto> input) {
        List<StrategyOrderDto> result = new ArrayList<>();
        for (SignalDto signal: input) {
            result.add(positionManagementService.createOrderForLongPosition(instrument, signal));
        }

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<TradingResultDto> backtest(String instrument, StrategyType strategyType, LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        TradingStatement result = backtestService.backtest(instrument, strategyType, dateTimeFormatter.format(fromDate), dateTimeFormatter.format(toDate));
        TradingResultDto resultDto = new TradingResultDto();
        resultDto.setBreakEvenCount(result.getPositionStatsReport().getBreakEvenCount().intValue());
        resultDto.setLossCount(result.getPositionStatsReport().getLossCount().intValue());
        resultDto.setProfitCount(result.getPositionStatsReport().getProfitCount().intValue());
        resultDto.setTotalProfitLoss(result.getPerformanceReport().getTotalProfitLoss().bigDecimalValue());
        resultDto.setTotalProfit(result.getPerformanceReport().getTotalProfit().bigDecimalValue());
        resultDto.setTotalLoss(result.getPerformanceReport().getTotalLoss().bigDecimalValue());
        resultDto.setTotalProfitLossPercentage(result.getPerformanceReport().getTotalProfitLossPercentage().bigDecimalValue());

        return ResponseEntity.ok(resultDto);
    }

    @Override
    public ResponseEntity<Object> getData(String instrument, LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return ResponseEntity.ok(marketDataService.loadRawData(instrument, dateTimeFormatter.format(fromDate), dateTimeFormatter.format(toDate)));
    }

    @Override
    public ResponseEntity<Object> getKallmanPrediction(String instrument, LocalDate fromDate, LocalDate toDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        BarSeries barSeries = marketDataService.loadHistoricalData(instrument, dateTimeFormatter.format(fromDate), dateTimeFormatter.format(toDate));
        barSeries.setMaximumBarCount(barSeries.getBarCount());
        ClosePriceIndicator close = new ClosePriceIndicator(barSeries);
        KallmanIndicator kallman = new KallmanIndicator(close, 0.1, 5);
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i=0;i< barSeries.getBarCount();i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("closePrice", close.getValue(i).bigDecimalValue());
            data.put("kallmanPrice", kallman.getValue(i).bigDecimalValue());
            data.put("% value", close.getValue(i).bigDecimalValue().divide(kallman.getValue(i).bigDecimalValue(), 2, RoundingMode.HALF_UP));

            result.add(data);
        }

        return ResponseEntity.ok(result);
    }
}
