package money.maker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.cache.InstrumentCache;
import money.maker.config.external.BarConfiguration;
import money.maker.dto.Tick;
import money.maker.dto.TickAggregator;
import money.maker.service.TickAggregatorService;
import org.springframework.stereotype.Service;
import org.ta4j.core.num.DoubleNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
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

        currentBarConcurrentMap.compute(token,
            (k, aggregator) -> getOrCreateNewTickAggregator(aggregator)
        );
    }

    private TickAggregator getOrCreateNewTickAggregator(TickAggregator aggregator) {
        if (null == aggregator) {
            int barDurationMilli = barConfiguration.getTimeFrame() * 1000;
            long currentTimeMilli = Instant.now().toEpochMilli();
            long barEndTimeMilli = ((currentTimeMilli / barDurationMilli) + 1) * barDurationMilli;

            aggregator = new TickAggregator(
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(barEndTimeMilli), ZoneId.of("UTC")),
                Duration.ofSeconds(barConfiguration.getTimeFrame()));
        }

        return aggregator;
    }

    @Override
    public void updateInstrument() {
        log.debug("clearing tick cache and updating instruments");
        currentBarConcurrentMap.forEach((token, tickAggregator) ->
            instrumentCache.updateInstrument(
                token,
                tickAggregator.asBar(DoubleNum::valueOf)
        ));
        currentBarConcurrentMap.clear();
    }
}
