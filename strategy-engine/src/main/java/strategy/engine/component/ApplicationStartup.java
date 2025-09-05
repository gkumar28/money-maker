package strategy.engine.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import strategy.engine.cache.BarDataCache;
import strategy.engine.constant.enums.StrategyType;
import strategy.engine.strategy.TradingStrategy;

@Component
@RequiredArgsConstructor
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final Portfolio portfolio;
    private final BarDataCache barDataCache;
    private final TradingStrategyFactory tradingStrategyFactory;
    private final TradingStrategyRegistry tradingStrategyRegistry;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (String instrument: portfolio.getInstruments()) {
            BarSeries instrumentData = barDataCache.get(instrument);
            TradingStrategy strategy = tradingStrategyFactory.create(StrategyType.LONG_TREND, instrumentData);
            tradingStrategyRegistry.register(instrument, strategy);
        }
    }
}
