package strategy.engine.service;

import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Portfolio;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioManagementService {

    void applyTrade(Portfolio portfolio, Trade trade);

    List<String> getInstruments();
}
