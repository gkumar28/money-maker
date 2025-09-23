package strategy.engine.service;

import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Portfolio;
import strategy.engine.schemaobject.Trade;
import strategy.engine.schemaobject.analysis.MultiLegPositionTradingRecord;
import strategy.engine.schemaobject.analysis.TradingRecord;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {
    void applyTrade(Trade trade, TradingRecord tradingRecord);

    BigDecimal getTotalValue();

    void init(BigDecimal totalCapital, BigDecimal availableCapital);

    void resetPortfolio(BigDecimal newCapital);

    Holding getCurrentHoldings(String instrument);

    List<String> getInstruments();

    Portfolio getPortfolio();

    void updateLastTradedPrice(String instrument, BigDecimal currentPrice);
}
