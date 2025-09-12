package strategy.engine.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import strategy.engine.cache.BarDataCache;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.schemaobject.PortfolioDto;
import strategy.engine.service.PortfolioService;
import strategy.engine.strategy.TradingStrategy;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final PortfolioService portfolioService;
    private final BarDataCache barDataCache;
    private final TradingStrategyFactory tradingStrategyFactory;
    private final TradingStrategyRegistry tradingStrategyRegistry;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (String instrument: portfolioService.getInstruments()) {
            log.debug("instantiating bar series for instrument: {}", instrument);
            BarSeries instrumentData = barDataCache.get(instrument);
            TradingStrategy strategy = tradingStrategyFactory.create(StrategyType.LONG_TREND, instrumentData);
            tradingStrategyRegistry.register(instrument, strategy);
        }
    }
}
