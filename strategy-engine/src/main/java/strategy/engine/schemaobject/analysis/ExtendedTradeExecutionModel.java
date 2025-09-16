package strategy.engine.schemaobject.analysis;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.TradeExecutionModel;
import org.ta4j.core.num.Num;
import strategy.engine.schemaobject.TradeDto;

public interface ExtendedTradeExecutionModel extends TradeExecutionModel {
    TradeDto execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount, Trade.TradeType tradeType);
}
