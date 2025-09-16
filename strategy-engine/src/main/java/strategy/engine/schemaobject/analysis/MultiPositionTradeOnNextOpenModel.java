package strategy.engine.schemaobject.analysis;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import strategy.engine.schemaobject.TradeDto;
import strategy.engine.util.StrategyEngineUtils;

public class MultiPositionTradeOnNextOpenModel implements ExtendedTradeExecutionModel {

    @Override
    public void execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount) {
        throw new UnsupportedOperationException("Trade type must be provided");
    }

    @Override
    public TradeDto execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Num amount, Trade.TradeType tradeType) {
        int indexOfExecutedBar = index + 1;
        if (indexOfExecutedBar <= barSeries.getEndIndex()) {
            if (Trade.TradeType.BUY.equals(tradeType)) {
                tradingRecord.enter(indexOfExecutedBar, barSeries.getBar(indexOfExecutedBar).getOpenPrice(), amount);
            } else {
                tradingRecord.exit(indexOfExecutedBar, barSeries.getBar(indexOfExecutedBar).getOpenPrice(), amount);
            }
            TradeDto executedTrade = new TradeDto();
            executedTrade.setInstrument(barSeries.getName());
            executedTrade.setIndex(indexOfExecutedBar);
            executedTrade.setDirection(StrategyEngineUtils.asTradeDirection(tradeType));
            executedTrade.setQuantity(amount.intValue());
            executedTrade.setPrice(barSeries.getBar(indexOfExecutedBar).getOpenPrice().bigDecimalValue());
            return executedTrade;
        }

        return null;
    }
}
