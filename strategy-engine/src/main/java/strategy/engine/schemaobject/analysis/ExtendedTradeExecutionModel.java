package strategy.engine.schemaobject.analysis;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.TradeExecutionModel;
import org.ta4j.core.num.Num;

public interface ExtendedTradeExecutionModel extends TradeExecutionModel {
    void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount, Trade.TradeType tradeType);
}
