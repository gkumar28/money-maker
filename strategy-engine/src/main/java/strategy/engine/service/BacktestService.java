package strategy.engine.service;

import strategy.engine.schemaobject.TradingReport;
import strategy.engine.strategy.StrategyDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BacktestService {
    TradingReport backtest(List<String> instruments, String exchange, String interval, LocalDate fromDate, LocalDate toDate);

    TradingReport backtest(List<String> instruments, String exchange, String interval, LocalDate fromDate, LocalDate toDate, StrategyDefinition strategyDefinition);

    List<Map<String, Object>> getIndicatorValues(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate);
}
