package strategy.engine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;
import strategy.engine.component.TradingStrategyFactory;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.schemaobject.BarData;
import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.service.BacktestService;
import strategy.engine.service.PositionManagementService;
import strategy.engine.strategy.TradingStrategy;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Profile("dev")
public class TestingApiController implements TestingApi {

    private final TradingStrategyFactory tradingStrategyFactory;
    private final PositionManagementService positionManagementService;
    private final BacktestService backtestService;

    @Override
    public ResponseEntity<List<Signal>> simulateSignals(String instrument, StrategyType type, List<BarData> input) {

        List<Signal> result = new ArrayList<>();
        BarSeries barSeries = new BaseBarSeries(instrument.toUpperCase() + ":" + type.toString());
        barSeries.setMaximumBarCount(100);
        TradingStrategy strategy = tradingStrategyFactory.create(type, barSeries);
        for (BarData barData : input) {
            barSeries.addBar(new BaseBar(
                Duration.of(barData.getDuration(), ChronoUnit.SECONDS),
                barData.getEndTime(),
                barData.getOpen(),
                barData.getHigh(),
                barData.getLow(),
                barData.getClose(),
                barData.getVolume(),
                barData.getAmount(),
                barData.getTrades(),
                DecimalNum::valueOf));

        }

        for (int i=0;i<input.size();i++) {
            Signal signal = strategy.evaluate(i);
            result.add(signal);
        }

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<Order>> simulateOrders(String instrument, List<Signal> input) {
        List<Order> result = new ArrayList<>();
        for (Signal signal: input) {
            result.add(positionManagementService.createOrderForLongPosition(instrument, signal));
        }

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<TradingReport> backtest(List<String> instruments, String exchange, String interval, StrategyType strategyType, LocalDate fromDate, LocalDate toDate) {
        TradingReport result = backtestService.backtest(instruments, exchange, interval, strategyType, fromDate, toDate);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Object> getKallmanPrediction(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        return ResponseEntity.ok(backtestService.getIndicatorValues(instrument, exchange, interval, fromDate, toDate));
    }
}
