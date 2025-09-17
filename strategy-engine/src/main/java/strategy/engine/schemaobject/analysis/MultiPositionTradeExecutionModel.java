package strategy.engine.schemaobject.analysis;

import org.ta4j.core.BarSeries;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.schemaobject.TradeDto;

public interface MultiPositionTradeExecutionModel {
    TradeDto execute(int index, MultiPositionTradingRecord tradingRecord, BarSeries barSeries, StrategyOrderDto strategyOrderDto);
}
