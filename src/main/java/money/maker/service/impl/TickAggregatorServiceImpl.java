package money.maker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.cache.InstrumentCache;
import money.maker.config.external.BarConfiguration;
import money.maker.dto.Tick;
import money.maker.dto.TickAggregator;
import money.maker.service.TickAggregatorService;
import org.springframework.stereotype.Service;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TickAggregatorServiceImpl implements TickAggregatorService {

    private final InstrumentCache instrumentCache;
    private final BarConfiguration barConfiguration;
    private final ConcurrentHashMap<String, TickAggregator> currentBarConcurrentMap = new ConcurrentHashMap<>();

    @Override
    public void processTick(Tick tick) {
        log.debug("updated tick cache");
        String token = tick.getInstrumentToken();
        currentBarConcurrentMap.compute(token, (k, existing) -> {
            if (existing == null) {
                existing = new TickAggregator(DecimalNum::valueOf);
            }
            existing.addTrade(tick.getVolume(), tick.getPrice());
            return existing;
        });
    }

    @Override
    public void updateInstrument() {
        log.debug("clearing tick cache and updating instruments");
        currentBarConcurrentMap.forEach((token, tickAggregator) ->
            instrumentCache.updateInstrument(
                token,
                tickAggregator.asBar(ZonedDateTime.now(),
                    Duration.ofSeconds(barConfiguration.getTimeFrame())))
        );
        currentBarConcurrentMap.clear();
    }
}
