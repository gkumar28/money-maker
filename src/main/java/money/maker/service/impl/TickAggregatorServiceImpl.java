package money.maker.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import money.maker.cache.InstrumentCache;
import money.maker.component.Portfolio;
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

    private final BarConfiguration barConfiguration;
    private final InstrumentCache instrumentCache;
    private final Portfolio portfolio;
    private final ConcurrentHashMap<String, TickAggregator> tickAggregatorMap = new ConcurrentHashMap<>();

    @Override
    public void processTick(Tick tick) {
        String token = tick.getSymbol();

        TickAggregator aggregator = tickAggregatorMap.get(token);
        if (null != aggregator) {
            aggregator.addTrade(tick.getVolume(), tick.getPrice());
        }
        log.debug("updated tick cache in time: {} ms", Instant.now().toEpochMilli() - tick.getTimestamp());
    }

    @Override
    public void updateInstrument() {
        log.debug("clearing tick cache and updating instruments");
        int barDurationMilli = barConfiguration.getTimeFrame() * 1000;
        long currentTimeMilli = Instant.now().toEpochMilli();
        long barEndTimeMilli = ((currentTimeMilli / barDurationMilli) + 1) * barDurationMilli;

        ZonedDateTime newEndTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(barEndTimeMilli), ZoneId.of("UTC"));
        for (String symbol : portfolio.getInstruments()) {

            TickAggregator oldAggregator = tickAggregatorMap.get(symbol);
            TickAggregator newAggregator = new TickAggregator(newEndTime, Duration.ofMillis(barDurationMilli));

            if (null == oldAggregator) {
                tickAggregatorMap.computeIfAbsent(symbol, k -> newAggregator);
            } else if (tickAggregatorMap.replace(symbol, oldAggregator, newAggregator) &&
                !oldAggregator.isEmpty()) {
                instrumentCache.updateInstrument(
                    symbol,
                    oldAggregator.asBar(DoubleNum::valueOf));
            }
        }
    }
}
