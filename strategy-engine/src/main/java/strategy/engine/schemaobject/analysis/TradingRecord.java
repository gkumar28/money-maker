package strategy.engine.schemaobject.analysis;

import strategy.engine.constant.enums.TradeType;
import strategy.engine.schemaobject.Position;
import strategy.engine.schemaobject.Trade;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface TradingRecord {

    TradeType getStartingType();

    String getName();

    Position getOpenPosition();

    List<Position> getClosedPositions();

    boolean buy(int index, Trade trade);

    boolean sell(int index, Trade trade);

    boolean operate(int index, Trade trade);

    List<Trade> getTrades();

    Trade getLastTrade();

    Trade getLastTrade(TradeType tradeType);

    Trade getLastEntry();

    Trade getLastExit();

    int getEntryTradeCount();

    int getExitTradeCount();

    int getBuyTradeCount();

    int getSellTradeCount();

    int getTradeCount();

    Integer getStartIndex();

    Integer getEndIndex();
}
