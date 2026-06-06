package strategy.engine.service;

import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Portfolio;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioManagementService {
    void applyTrade(Portfolio portfolio, Trade trade);

    BigDecimal getTotalValue(Portfolio portfolio);

    void init(Portfolio portfolio, BigDecimal totalCapital, BigDecimal availableCapital);

    void resetPortfolio(Portfolio portfolio, BigDecimal newCapital);

    Holding getCurrentHoldings(Portfolio portfolio, String instrument);

    List<String> getInstruments();

    void updateLastTradedPrice(Portfolio portfolio, String instrument, BigDecimal currentPrice);
}
