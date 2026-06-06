package strategy.engine.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Trade;
import strategy.engine.schemaobject.Holding;
import strategy.engine.schemaobject.Portfolio;
import strategy.engine.service.PortfolioManagementService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static strategy.engine.util.StrategyEngineUtils.sanitize;

@Service
@Slf4j
public class PortfolioManagementServiceImpl implements PortfolioManagementService {


    @Override
    public BigDecimal getTotalValue(Portfolio portfolio) {
        return portfolio.getAvailableCapital();
    }

    @Override
    public void init(Portfolio portfolio, BigDecimal initialCapital, BigDecimal availableCapital) {
        portfolio.setInitialCapital(initialCapital);
        portfolio.setAvailableCapital(availableCapital);
        portfolio.getTpPrices().clear();
        portfolio.getSlPrices().clear();
    }

    @Override
    public void resetPortfolio(Portfolio portfolio, BigDecimal newCapital) {
        portfolio.getHoldings().clear();
        portfolio.setAvailableCapital(newCapital);
        portfolio.setRealizedPnL(BigDecimal.ZERO);
        portfolio.setInitialCapital(newCapital);
        portfolio.setInvestedCapital(BigDecimal.ZERO);
        portfolio.setMaxInvestedCapital(BigDecimal.ZERO);
        portfolio.getTpPrices().clear();
        portfolio.getSlPrices().clear();
    }

    @Override
    public Holding getCurrentHoldings(Portfolio portfolio, String instrument) {
        return portfolio.getHolding(instrument);
    }

    @Override
    public List<String> getInstruments() {
        return List.of("RELIANCE");
    }

    // TO-DO: move this to market data layer and devise a way to construct current value using holding and market data
    @Override
    public void updateLastTradedPrice(Portfolio portfolio, String instrument, BigDecimal currentPrice) {
        Holding holding = getCurrentHoldings(portfolio, instrument);
        if (holding.quantity().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        portfolio.getHoldings().put(instrument, holding.withValue(currentPrice.multiply(holding.quantity())));
    }

    @Override
    public void applyTrade(Portfolio portfolio, Trade trade) {
        if (trade == null || trade.getAmount().bigDecimalValue().compareTo(BigDecimal.ZERO) == 0) return;

        String instrument = trade.getInstrument();
        Portfolio.Snapshot portfolioSnapshot = portfolio.applyTrade(trade);
        Holding holding = portfolioSnapshot.holdings().getOrDefault(instrument, Holding.instance(instrument));
        log.debug("{}: invested capital: {} Avg Entry Price: {} Quantity: {}",
                holding.instrument(),
                sanitize(holding.investedCapital()),
                sanitize(holding.avgEntryPrice()),
                sanitize(holding.quantity()));
    }
}
