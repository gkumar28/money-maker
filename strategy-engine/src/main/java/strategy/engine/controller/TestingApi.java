package strategy.engine.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.schemaobject.BarData;
import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.TradingReport;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/test")
public interface TestingApi {
    @PostMapping("/run-back-test")
    ResponseEntity<TradingReport> backtest(@RequestParam List<String> instruments,
                                           @RequestParam String exchange,
                                           @RequestParam String interval,
                                           @RequestParam StrategyType strategyType,
                                           @RequestParam LocalDate fromDate,
                                           @RequestParam LocalDate toDate);

    @GetMapping("/indicators/kallman")
    ResponseEntity<Object> getKallmanPrediction(@RequestParam String instrument,
                                                @RequestParam String exchange,
                                                @RequestParam String interval,
                                                @RequestParam LocalDate fromDate,
                                                @RequestParam LocalDate toDate);
}
