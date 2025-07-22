package money.maker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.cache.InstrumentCache;
import money.maker.config.external.CandleConfiguration;
import money.maker.dto.Candle;
import money.maker.dto.Tick;
import money.maker.service.CandleAggregatorService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleAggregatorServiceImpl implements CandleAggregatorService {

    private final CandleConfiguration candleConfiguration;
    private final InstrumentCache instrumentCache;
    private final ConcurrentHashMap<String, Candle> candleConcurrentHashMap = new ConcurrentHashMap<>();

    @Override
    public void processTick(Tick tick) {
        log.debug("updated tick cache");
        String token = tick.getInstrumentToken();
        candleConcurrentHashMap.compute(token, (k, existing) -> {
            if (existing == null) {
                existing = new Candle(token);
            }
            existing.applyTick(tick);
            return existing;
        });
    }

    @Override
    public void updateInstrument(int longMaPeriod, int shortMaPeriod, int rsiPeriod) {
        log.debug("clearing tick cache and updating instruments");
        candleConcurrentHashMap.forEach((token, candle) -> {
            instrumentCache.updateInstrument(token, candle, longMaPeriod, shortMaPeriod, rsiPeriod);
        });
        candleConcurrentHashMap.clear();
    }
}
