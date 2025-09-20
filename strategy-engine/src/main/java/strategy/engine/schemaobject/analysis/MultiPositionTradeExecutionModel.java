package strategy.engine.schemaobject.analysis;

import org.ta4j.core.BarSeries;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.Trade;

public interface MultiPositionTradeExecutionModel {
    Trade execute(int index, MultiPositionTradingRecord tradingRecord, BarSeries barSeries, Order order);
}
