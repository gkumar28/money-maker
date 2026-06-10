package sre.engine.strategy.service.impl;

import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import sre.engine.strategy.cache.BarDataCache;
import sre.engine.strategy.cache.TradeSignalCache;
import sre.engine.strategy.component.PortfolioContainer;
import common.lib.schemaobjects.Registry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sre.engine.strategy.schemaobject.signal.SignalContext;
import sre.engine.strategy.service.SignalGenerationService;
import sre.engine.strategy.service.RedisService;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import sre.engine.strategy.service.RiskManagementService;
import sre.engine.strategy.strategy.SignalStrategy;

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
