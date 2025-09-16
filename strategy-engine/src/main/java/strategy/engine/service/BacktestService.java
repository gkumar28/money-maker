package strategy.engine.service;

import org.ta4j.core.reports.TradingStatement;
import strategy.engine.constant.enums.StrategyType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BacktestService {
    TradingStatement backtest(String instrument, String exchange, String interval, StrategyType strategyType, LocalDate fromDate, LocalDate toDate);

    List<Map<String, Object>> getIndicatorValues(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate);
}
