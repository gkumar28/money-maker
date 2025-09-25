package strategy.engine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import strategy.engine.factory.TradingStrategyFactory;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.service.BacktestService;
import strategy.engine.service.PositionManagementService;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Profile("dev")
public class TestingApiController implements TestingApi {

    private final TradingStrategyFactory tradingStrategyFactory;
    private final PositionManagementService positionManagementService;
    private final BacktestService backtestService;

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
