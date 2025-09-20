package strategy.engine.service.impl;

import org.ta4j.core.BarSeries;
import strategy.engine.cache.BarDataCache;
import strategy.engine.cache.TradeSignalCache;
import strategy.engine.component.TradingStrategyRegistry;
import strategy.engine.schemaobject.Signal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import strategy.engine.schemaobject.Order;
import strategy.engine.service.SignalGenerationService;
import strategy.engine.service.RedisService;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import strategy.engine.service.PositionManagementService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalGenerationServiceImpl implements SignalGenerationService {

    private final BarDataCache barDataCache;
    private final RedisService redisService;
    private final TradeSignalCache tradeSignalCache;
    private final TradingStrategyRegistry strategyRegistry;
    private final PositionManagementService positionManagementService;

    @Override
    public void onNewBarEvent(String instrument, Bar bar) {
        BarSeries barSeries = barDataCache.updateAndGetInstrument(instrument, bar);
        Signal newSignal = strategyRegistry.get(instrument).evaluate(barSeries.getEndIndex());
        Order order = positionManagementService.createOrderForLongPosition(instrument, newSignal);

        tradeSignalCache.update(instrument, newSignal);
        if (null == newSignal.getDirection()) {
            // if HOLD then no event
            return;
        }

        redisService.raiseSignalEvent(instrument, newSignal);
        redisService.raiseOrderEvent(order);
    }
}
