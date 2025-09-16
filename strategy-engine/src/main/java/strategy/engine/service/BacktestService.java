package strategy.engine.service;

import strategy.engine.constant.enums.StrategyType;
import strategy.engine.schemaobject.TradingReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BacktestService {
    TradingReport backtest(List<String> instruments, String exchange, String interval, StrategyType strategyType, LocalDate fromDate, LocalDate toDate);

    List<Map<String, Object>> getIndicatorValues(String instrument, String exchange, String interval, LocalDate fromDate, LocalDate toDate);
}
