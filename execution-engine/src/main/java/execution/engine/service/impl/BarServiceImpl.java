package execution.engine.service.impl;

import execution.engine.cache.BarDataCache;
import execution.engine.strategy.TradingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import execution.engine.service.BarService;
import execution.engine.service.RedisService;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarServiceImpl implements BarService {

    private final BarDataCache barDataCache;
    private final RedisService redisService;
    private final List<TradingStrategy> tradingStrategyList;

    @Override
    public final void onNewBarEvent(String instrument, String timestamp) {
        Bar bar = redisService.getBar(instrument, timestamp);
        BarSeries instrumentData = barDataCache.updateAndGetInstrument(instrument, bar);
    }
}
