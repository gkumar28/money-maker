package sre.engine.strategy.service;

import org.ta4j.core.reports.TradingStatement;
import sre.engine.strategy.strategy.StrategyDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StrategyBacktestService {
    TradingStatement backtest(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate);

    TradingStatement backtest(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate, boolean writeResultToFile, StrategyDefinition strategyDefinition);

    List<Map<String, Object>> getIndicatorValues(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate);
}
