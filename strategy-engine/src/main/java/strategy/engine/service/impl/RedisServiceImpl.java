package strategy.engine.service.impl;

import org.ta4j.core.bars.TimeBarBuilder;
import org.ta4j.core.num.DecimalNumFactory;
import org.ta4j.core.num.NumFactory;
import strategy.engine.config.external.BarConfiguration;
import strategy.engine.schemaobject.signal.SignalContext;
import strategy.engine.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static strategy.engine.constant.ApplicationConstants.BAR;
import static strategy.engine.constant.ApplicationConstants.DATA;
import static strategy.engine.constant.ApplicationConstants.DELIMITER_DOT;
import static strategy.engine.constant.ApplicationConstants.SIGNAL;
import static strategy.engine.constant.ApplicationConstants.TIMESTAMP;
import static strategy.engine.util.StrategyEngineUtils.sanitize;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final BarConfiguration barConfiguration;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Bar getBar(String instrument, String timestamp) {
        String dataKey = getKey(BAR, DATA, instrument);

        // Step 2: Get CSV string from hash
        Object value = redisTemplate.opsForHash().get(dataKey, timestamp);
        return getBar(instrument, value, Instant.ofEpochMilli(Long.parseLong(timestamp)));
    }

    @Override
    public BarSeries getAllBars(String instrument, List<String> timestamps) {
        String dataKey = getKey(BAR, DATA, instrument);

        // Step 2: Get CSV string from hash
        List<Object> values = redisTemplate.opsForHash().multiGet(dataKey, Arrays.asList(timestamps.toArray()));
        BarSeries result = new BaseBarSeries(instrument, List.of());
        for(int i=0;i<values.size();i++) {
            result.addBar(getBar(instrument, values.get(i), Instant.ofEpochMilli(Long.parseLong(timestamps.get(i)))));
        }

        return result;
    }

    @Override
    public BarSeries getNBars(String instrument, int n, int offset) {
        String timestampKey = getKey(BAR, TIMESTAMP, instrument);
        Set<String> timestamps = redisTemplate.opsForZSet().range(timestampKey, offset, n - 1L + offset);

        if (null == timestamps) {
            return new BaseBarSeries(instrument, List.of());
        }

        List<String> timestampList = new ArrayList<>(timestamps);
        timestampList.sort(Comparator.comparingLong(Long::parseLong));
        return getAllBars(instrument, timestampList);
    }

    @Override
    public void raiseSignalEvent(String instrument, SignalContext signalContext) {
        String channelName = getKey(SIGNAL, instrument);
        String value = toSignalString(signalContext);
        redisTemplate.convertAndSend(channelName, value);
    }

    private String toSignalString(SignalContext signalContext) {
        return String.format("%s,%d,%s",
                sanitize(signalContext.signal().exposure()),
            Instant.from(signalContext.signal().timestamp()).toEpochMilli(),
            signalContext.metaData().price().toPlainString());
    }
    private Bar getBar(String instrument, Object data, Instant endTime) {
        log.debug("data for instrument {}, timestamp {} : {}", instrument, endTime, data);
        if (null == data) {
            return new TimeBarBuilder()
                    .timePeriod(Duration.of(barConfiguration.getTimeFrame(), ChronoUnit.SECONDS))
                    .endTime(endTime)
                    .build();
        }
        String[] parts = data.toString().split(",");
        return new TimeBarBuilder()
            .timePeriod(Duration.of(barConfiguration.getTimeFrame(), ChronoUnit.SECONDS))
            .endTime(endTime)
            .openPrice(parts[0])
            .highPrice(parts[1])
            .lowPrice(parts[2])
            .closePrice(parts[3])
            .volume(parts[4])
            .amount(parts[5])
            .trades(parts[6])
            .build();
    }

    private String getKey(String keySpace, String subKeySpace, String key) {
        return String.join(DELIMITER_DOT, keySpace, subKeySpace, key);
    }

    private String getKey(String keySpace, String key) {
        return String.join(DELIMITER_DOT, keySpace, key);
    }
}
