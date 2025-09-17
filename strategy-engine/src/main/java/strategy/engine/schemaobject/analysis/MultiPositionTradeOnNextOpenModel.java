package strategy.engine.schemaobject.analysis;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import strategy.engine.constant.enums.TradeDirection;
import strategy.engine.schemaobject.StrategyOrderDto;
import strategy.engine.schemaobject.TradeDto;

@Slf4j
public class MultiPositionTradeOnNextOpenModel implements MultiPositionTradeExecutionModel {

    @Override
    public TradeDto execute(int index, MultiPositionTradingRecord tradingRecord, BarSeries barSeries, StrategyOrderDto strategyOrderDto) {
        int indexOfExecutedBar = index + 1;
        if (indexOfExecutedBar <= barSeries.getEndIndex()) {
            if (TradeDirection.BUY.equals(strategyOrderDto.getDirection())) {
                tradingRecord.enter(indexOfExecutedBar, barSeries.getBar(indexOfExecutedBar).getOpenPrice(), DecimalNum.valueOf(strategyOrderDto.getQuantity()));
            } else {
                tradingRecord.exit(indexOfExecutedBar, barSeries.getBar(indexOfExecutedBar).getOpenPrice(), DecimalNum.valueOf(strategyOrderDto.getQuantity()));
            }
            log.debug("{}: Trade executed index: {} quantity: {} price: {}", strategyOrderDto.getInstrument(), index, strategyOrderDto.getQuantity(), barSeries.getBar(indexOfExecutedBar).getOpenPrice());
            TradeDto executedTrade = new TradeDto();
            executedTrade.setInstrument(barSeries.getName());
            executedTrade.setIndex(indexOfExecutedBar);
            executedTrade.setDirection(strategyOrderDto.getDirection());
            executedTrade.setQuantity(strategyOrderDto.getQuantity());
            executedTrade.setPrice(barSeries.getBar(indexOfExecutedBar).getOpenPrice().bigDecimalValue());
            executedTrade.setTimestamp(strategyOrderDto.getTimestamp());
            return executedTrade;
        }

        return null;
    }
}
