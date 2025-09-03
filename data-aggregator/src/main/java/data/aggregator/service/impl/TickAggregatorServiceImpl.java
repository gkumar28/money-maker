package data.aggregator.service.impl;

import data.aggregator.config.external.SchedulerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import data.aggregator.component.Portfolio;
import data.aggregator.dto.Tick;
import data.aggregator.dto.TickAggregator;
import data.aggregator.service.RedisService;
import data.aggregator.service.TickAggregatorService;
import org.springframework.scheduling.annotation.Async;
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

    private final SchedulerConfiguration schedulerConfiguration;
    private final Portfolio portfolio;
    private final RedisService redisService;
    private final ConcurrentHashMap<String, TickAggregator> tickAggregatorMap = new ConcurrentHashMap<>();

    @Override
    @Async("taskExecutor")
    public void processTick(Tick tick) {
        String instrument = tick.getInstrument();

        TickAggregator aggregator = tickAggregatorMap.get(instrument);
        if (null != aggregator) {
            aggregator.addTrade(tick.getVolume(), tick.getPrice());
        }
        log.debug("updated tick cache in time: {} ms", Instant.now().toEpochMilli() - tick.getTimestamp());
    }

    @Override
    public void updateInstrument() {
        log.debug("clearing tick cache and raising new Bar event");
        int barDurationMilli = schedulerConfiguration.getTaskCronInterval() * 1000;
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
                redisService.raiseNewBarEvent(symbol, oldAggregator.asBar(DoubleNum::valueOf));
            }
        }
    }
}
