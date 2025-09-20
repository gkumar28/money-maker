package strategy.engine.schemaobject.analysis;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import strategy.engine.constant.enums.TradeDirection;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.Trade;

@Slf4j
public class MultiPositionTradeOnNextOpenModel implements MultiPositionTradeExecutionModel {

    @Override
    public Trade execute(int index, MultiPositionTradingRecord tradingRecord, BarSeries barSeries, Order order) {
        int indexOfExecutedBar = index + 1;
        if (indexOfExecutedBar <= barSeries.getEndIndex()) {
            if (TradeDirection.BUY.equals(order.getDirection())) {
                tradingRecord.enter(indexOfExecutedBar, barSeries.getBar(indexOfExecutedBar).getOpenPrice(), DecimalNum.valueOf(order.getQuantity()));
            } else {
                tradingRecord.exit(indexOfExecutedBar, barSeries.getBar(indexOfExecutedBar).getOpenPrice(), DecimalNum.valueOf(order.getQuantity()));
            }
            log.debug("{}: Trade executed index: {} quantity: {} price: {}", order.getInstrument(), index, order.getQuantity(), barSeries.getBar(indexOfExecutedBar).getOpenPrice());
            Trade executedTrade = new Trade();
            executedTrade.setInstrument(barSeries.getName());
            executedTrade.setIndex(indexOfExecutedBar);
            executedTrade.setDirection(order.getDirection());
            executedTrade.setQuantity(order.getQuantity());
            executedTrade.setPrice(barSeries.getBar(indexOfExecutedBar).getOpenPrice().bigDecimalValue());
            executedTrade.setTimestamp(order.getTimestamp());
            return executedTrade;
        }

        return null;
    }
}
