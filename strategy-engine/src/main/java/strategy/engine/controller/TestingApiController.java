package strategy.engine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.ta4j.core.backtest.BacktestExecutionResult;
import org.ta4j.core.reports.TradingStatement;
import strategy.engine.schemaobject.TradingReport;
import strategy.engine.service.AsyncService;
import strategy.engine.service.StrategyBacktestService;
import strategy.engine.strategy.StrategyDefinition;
import strategy.engine.watchmaker.StrategyDefinitionEvolution;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Future;

@Controller
@RequiredArgsConstructor
@Profile("dev")
public class TestingApiController implements TestingApi {

    private final AsyncService asyncService;
    private final StrategyBacktestService strategyBacktestService;

    @Override
    public ResponseEntity<TradingStatement> backtest(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        TradingStatement result = strategyBacktestService.backtest(instrument, exchange, interval, fromDate, toDate);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Object> getKallmanPrediction(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        return ResponseEntity.ok(strategyBacktestService.getIndicatorValues(instrument, exchange, interval, fromDate, toDate));
    }

    @Override
    public ResponseEntity<Future<StrategyDefinition>> evolve(List<String> instruments, String exchange, String interval, LocalDate fromDate, LocalDate toDate) {
        StrategyDefinitionEvolution evolution = new StrategyDefinitionEvolution(strategyBacktestService, instruments, exchange, interval, fromDate, toDate);

        return ResponseEntity.ok(asyncService.run(evolution::evolve));
    }
}
