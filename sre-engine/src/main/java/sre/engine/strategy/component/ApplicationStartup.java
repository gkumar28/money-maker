package sre.engine.strategy.component;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import sre.engine.strategy.cache.BarDataCache;
import sre.engine.strategy.schemaobject.Portfolio;
import sre.engine.strategy.service.PortfolioManagementService;
import sre.engine.strategy.strategy.StrategyDefinition;
import sre.engine.strategy.strategy.StrategyDefinitionParser;
import sre.engine.strategy.strategy.DiscreteSignalStrategy;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final PortfolioManagementService portfolioManagementService;
    private final BarDataCache barDataCache;
    private final PortfolioContainer portfolioContainer;
    private final StrategyDefinitionParser strategyDefinitionParser;
    private final Registry<Strategy> strategyRegistry;

    @Override
    public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {

        StrategyDefinition strategyDefinition = strategyDefinitionParser.readAny();
        if (null == strategyDefinition) {
            return;
        }
        portfolioContainer.setPortfolio(new Portfolio()); // TO BE: need to store portfolio in durable location
        for (String instrument: portfolioManagementService.getInstruments()) {
            log.debug("instantiating strategy for instrument: {}", instrument);
            BarSeries instrumentData = barDataCache.get(instrument);
            TradingRecord tradingRecord = new BaseTradingRecord(instrument, Trade.TradeType.BUY);
            DiscreteSignalStrategy DiscreteSignalStrategy = new DiscreteSignalStrategy(strategyDefinition, instrumentData);
            strategyRegistry.register(instrument, DiscreteSignalStrategy);
        }
    }
}
