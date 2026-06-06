package strategy.engine.service.impl;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import strategy.engine.cache.BarDataCache;
import strategy.engine.cache.TradeSignalCache;
import strategy.engine.component.PortfolioContainer;
import strategy.engine.component.Registry;
import strategy.engine.schemaobject.signal.Signal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import strategy.engine.schemaobject.Order;
import strategy.engine.schemaobject.signal.SignalContext;
import strategy.engine.service.SignalGenerationService;
import strategy.engine.service.RedisService;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import strategy.engine.service.RiskManagementService;
import strategy.engine.strategy.SignalStrategy;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalGenerationServiceImpl implements SignalGenerationService {

    private final BarDataCache barDataCache;
    private final RedisService redisService;
    private final TradeSignalCache tradeSignalCache;
    private final Registry<SignalStrategy> strategyRegistry;
    private final Registry<TradingRecord> tradingRecordRegistry;
    private final RiskManagementService riskManagementService;
    private final PortfolioContainer portfolioContainer;

    @Override
    public void onNewBarEvent(String instrument, Bar bar) {
        BarSeries barSeries = barDataCache.updateAndGetInstrument(instrument, bar);
        SignalContext rawSignal = strategyRegistry.get(instrument).operate(barSeries.getEndIndex(), tradingRecordRegistry.get(instrument));
        SignalContext finalSignal = riskManagementService.createOrder(portfolioContainer.getPortfolio(), instrument, rawSignal);

        tradeSignalCache.update(instrument, finalSignal.signal());
        if (finalSignal.signal().shouldBeDiscarded()) {
            // if HOLD then no event
            return;
        }

        redisService.raiseSignalEvent(instrument, finalSignal);
    }
}
