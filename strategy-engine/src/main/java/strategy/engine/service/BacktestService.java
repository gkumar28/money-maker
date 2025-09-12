package strategy.engine.service;

import org.ta4j.core.reports.TradingStatement;
import strategy.engine.constant.enums.StrategyType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BacktestService {
    TradingStatement backtest(String instrument, StrategyType strategyType, String fromDate, String toDate);

    List<Map<String, Object>> getIndicatorValues(String instrument, LocalDate fromDate, LocalDate toDate);
}
