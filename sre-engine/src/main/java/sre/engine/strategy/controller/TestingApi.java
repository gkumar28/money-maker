package sre.engine.strategy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.ta4j.core.reports.TradingStatement;
import sre.engine.strategy.strategy.StrategyDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Future;

@RequestMapping("/test")
public interface TestingApi {
    @PostMapping("/run-back-test")
    ResponseEntity<TradingStatement> backtest(@RequestParam String instrument,
                                              @RequestParam String exchange,
                                              @RequestParam String interval,
                                              @RequestParam LocalDate fromDate,
                                              @RequestParam LocalDate toDate);

    @GetMapping("/indicators/kallman")
    ResponseEntity<Object> getKallmanPrediction(@RequestParam String instrument,
                                                @RequestParam String exchange,
                                                @RequestParam String interval,
                                                @RequestParam LocalDate fromDate,
                                                @RequestParam LocalDate toDate);


    @PostMapping("/run-evolution-sim")
    ResponseEntity<Future<StrategyDefinition>> evolve(@RequestParam String instrument,
                                                      @RequestParam String exchange,
                                                      @RequestParam String interval,
                                                      @RequestParam LocalDate fromDate,
                                                      @RequestParam LocalDate toDate);
}
