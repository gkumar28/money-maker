package sre.engine.strategy.service;

import org.ta4j.core.Trade;
import sre.engine.strategy.schemaobject.Portfolio;

import java.util.List;

public interface PortfolioManagementService {

    void applyTrade(Portfolio portfolio, Trade trade);

    List<String> getInstruments();
}
