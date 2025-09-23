package strategy.engine.schemaobject.analysis;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import strategy.engine.constant.enums.TradeType;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.Trade;

import static strategy.engine.util.StrategyEngineUtils.sanitize;

@Slf4j
public class TradeOnNextOpenModel extends TradeExecutionModel {

    public TradeOnNextOpenModel(Cost cost) {
        super(cost);
    }

    public TradeOnNextOpenModel(Cost entryCost, Cost exitCost) {
        super(entryCost, exitCost);
    }

    @Override
    public Trade execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Order order) {
        int indexOfExecutedBar = index + 1;

        if (indexOfExecutedBar <= barSeries.getEndIndex()) {
            Trade executedTrade = Trade.builder()
                .instrument(order.getInstrument())
                .tradeType(order.getTradeType())
                .index(indexOfExecutedBar)
                .timestamp(barSeries.getBar(indexOfExecutedBar).getEndTime())
                .price(barSeries.getBar(indexOfExecutedBar).getOpenPrice().bigDecimalValue())
                .quantity(order.getQuantity())
                .transactionCost(getCost(tradingRecord.getStartingType(), order.getTradeType()))
                .build();
            tradingRecord.operate(indexOfExecutedBar, executedTrade);
            log.debug("{}: Trade executed index: {} quantity: {} price: {}", order.getInstrument(), index, sanitize(order.getQuantity()), barSeries.getBar(indexOfExecutedBar).getOpenPrice());

            return executedTrade;
        }

        return null;
    }

    private Cost getCost(TradeType positionEntryType, TradeType orderType) {
        if (positionEntryType == null) {
            // No current position → use order direction only
            return orderType == TradeType.BUY ? buyLong : sellShort;
        }

        if (positionEntryType == TradeType.BUY && orderType == TradeType.SELL) {
            return sellLong; // exit long
        } else if (positionEntryType == TradeType.SELL && orderType == TradeType.BUY) {
            return buyShort; // exit Short
        } else if (positionEntryType == TradeType.BUY && orderType == TradeType.BUY) {
            return buyLong; // enter long
        } else if (positionEntryType == TradeType.SELL && orderType == TradeType.SELL) {
            return sellShort; // enter short
        }

        return null;
    }
}
