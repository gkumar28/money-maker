package strategy.engine.schemaobject.analysis;

import org.ta4j.core.BarSeries;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.Trade;

public abstract class TradeExecutionModel {
    protected final Cost buyLong;
    protected final Cost sellLong;
    protected final Cost buyShort;
    protected final Cost sellShort;

    TradeExecutionModel(Cost cost) {
        this.buyLong = cost;
        this.sellLong = cost;
        this.buyShort = cost;
        this.sellShort = cost;
    }

    TradeExecutionModel(Cost entryCost, Cost exitCost) {
        this.buyLong = entryCost;
        this.sellLong = exitCost;
        this.buyShort = exitCost;
        this.sellShort = entryCost;
    }

    public abstract Trade execute(int index, TradingRecord tradingRecord, BarSeries barSeries, Order order);
}
