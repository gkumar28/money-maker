package strategy.engine.service;

import org.ta4j.core.reports.TradingStatement;
import strategy.engine.constant.enums.StrategyType;

public interface BacktestService {
    TradingStatement backtest(String instrument, StrategyType strategyType, String fromDate, String toDate);
}
