package sre.engine.strategy.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.Trade;
import common.lib.schemaobjects.Holding;
import common.lib.schemaobjects.Portfolio;
import sre.engine.strategy.service.PortfolioManagementService;

import java.math.BigDecimal;
import java.util.List;

import static common.lib.utils.GenericUtils.sanitize;

@Service
@Slf4j
public class PortfolioManagementServiceImpl implements PortfolioManagementService {

    @Override
    public List<String> getInstruments() {
        return List.of("RELIANCE");
    }

    @Override
    public void applyTrade(Portfolio portfolio, Trade trade) {
        if (trade == null || trade.getAmount().bigDecimalValue().compareTo(BigDecimal.ZERO) == 0) return;

        String instrument = trade.getInstrument();
        portfolio.applyTrade(trade);
        Portfolio.Snapshot portfolioSnapshot = portfolio.snapshot();
        Holding holding = portfolioSnapshot.holdings().getOrDefault(instrument, Holding.instance(instrument));
        log.debug("{}: invested capital: {} Avg Entry Price: {} Quantity: {}",
                holding.instrument(),
                sanitize(holding.investedCapital()),
                sanitize(holding.avgEntryPrice()),
                sanitize(holding.quantity()));
    }
}
