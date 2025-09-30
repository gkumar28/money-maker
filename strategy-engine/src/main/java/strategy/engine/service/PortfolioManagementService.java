package strategy.engine.service;

import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Portfolio;
import strategy.engine.schemaobject.Trade;
import strategy.engine.schemaobject.analysis.TradingRecord;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioManagementService {
    void applyTrade(Portfolio portfolio, Trade trade, TradingRecord tradingRecord);

    BigDecimal getTotalValue(Portfolio portfolio);

    void init(Portfolio portfolio, BigDecimal totalCapital, BigDecimal availableCapital);

    void resetPortfolio(Portfolio portfolio, BigDecimal newCapital);

    Holding getCurrentHoldings(Portfolio portfolio, String instrument);

    List<String> getInstruments();

    void updateLastTradedPrice(Portfolio portfolio, String instrument, BigDecimal currentPrice);
}
