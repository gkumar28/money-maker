package strategy.engine.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.ta4j.core.reports.TradingStatement;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.schemaobject.BarDataDto;
import strategy.engine.schemaobject.SignalDto;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.schemaobject.TradingResultDto;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/test")
public interface TestingApi {

    @PostMapping("/simulate-signals")
    ResponseEntity<List<SignalDto>> simulateSignals(@RequestParam @NotEmpty String instrument,
                                                    @RequestParam @NotEmpty StrategyType strategy,
                                                    @RequestBody @NotEmpty @Valid List<BarDataDto> input);

    @PostMapping("/simulate-orders")
    ResponseEntity<List<StrategyOrderDto>> simulateOrders(@RequestParam String instrument, @RequestBody @NotEmpty List<SignalDto> input);

    @PostMapping("/run-back-test")
    ResponseEntity<TradingResultDto> backtest(@RequestParam String instrument, @RequestParam StrategyType strategyType, @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate);

    @GetMapping("/data")
    ResponseEntity<Object> getData(@RequestParam String instrument, @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate);

    @GetMapping("/indicators/kallman")
    ResponseEntity<Object> getKallmanPrediction(@RequestParam String instrument, @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate);
}
