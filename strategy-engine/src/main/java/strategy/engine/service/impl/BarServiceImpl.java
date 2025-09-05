package strategy.engine.service.impl;

import strategy.engine.cache.BarDataCache;
import strategy.engine.cache.TradeSignalCache;
import strategy.engine.component.TradingStrategyRegistry;
import strategy.engine.constant.enums.TradeSignal;
import strategy.engine.schemaobject.SignalState;
import strategy.engine.strategy.TradingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import strategy.engine.service.BarService;
import strategy.engine.service.RedisService;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarServiceImpl implements BarService {

    private final BarDataCache barDataCache;
    private final RedisService redisService;
    private final TradeSignalCache tradeSignalCache;
    private final TradingStrategyRegistry strategyRegistry;

    @Override
    public final void onNewBarEvent(String instrument, String timestamp) {
        Bar bar = redisService.getBar(instrument, timestamp);
        barDataCache.updateInstrument(instrument, bar);
        SignalState newSignal = new SignalState(TradeSignal.HOLD, bar.getEndTime(), bar.getClosePrice().bigDecimalValue());

        TradingStrategy strategy = strategyRegistry.get(instrument);
        if (strategy.shouldEnter()) {
            newSignal.setSignal(TradeSignal.ENTRY);
        }
        if (strategy.shouldExit()) {
            newSignal.setSignal(TradeSignal.EXIT);
        }

        tradeSignalCache.update(instrument, newSignal);
        redisService.raiseSignalEvent(instrument, newSignal);
    }
}
