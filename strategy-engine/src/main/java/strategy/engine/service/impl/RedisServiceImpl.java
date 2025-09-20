package strategy.engine.service.impl;

import strategy.engine.config.external.BarConfiguration;
import strategy.engine.schemaobject.Signal;
import strategy.engine.schemaobject.Order;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
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
import static strategy.engine.constant.ApplicationConstants.ORDER;
import static strategy.engine.constant.ApplicationConstants.SIGNAL;
import static strategy.engine.constant.ApplicationConstants.TIMESTAMP;

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
        ZonedDateTime endTime = Instant.ofEpochMilli(Long.parseLong(timestamp)).atZone(ZoneId.of("UTC"));
        return getBar(instrument, value, endTime);
    }

    @Override
    public BarSeries getAllBars(String instrument, List<String> timestamps) {
        String dataKey = getKey(BAR, DATA, instrument);

        // Step 2: Get CSV string from hash
        List<Object> values = redisTemplate.opsForHash().multiGet(dataKey, Arrays.asList(timestamps.toArray()));
        BarSeries result = new BaseBarSeries(instrument, DecimalNum.ZERO);
        for(int i=0;i<values.size();i++) {
            ZonedDateTime endTime = Instant.ofEpochMilli(Long.parseLong(timestamps.get(i)))
                .atZone(ZoneId.of("UTC"));
            result.addBar(getBar(instrument, values.get(i), endTime));
        }

        return result;
    }

    @Override
    public BarSeries getNBars(String instrument, int n, int offset) {
        String timestampKey = getKey(BAR, TIMESTAMP, instrument);
        Set<String> timestamps = redisTemplate.opsForZSet().range(timestampKey, offset, n - 1L + offset);

        if (null == timestamps) {
            return new BaseBarSeries();
        }

        List<String> timestampList = new ArrayList<>(timestamps);
        timestampList.sort(Comparator.comparingLong(Long::parseLong));
        return getAllBars(instrument, timestampList);
    }

    @Override
    public void raiseSignalEvent(String instrument, Signal signal) {
        String channelName = getKey(SIGNAL, instrument);
        String value = toSignalString(signal);
        redisTemplate.convertAndSend(channelName, value);
    }

    @Override
    public void raiseOrderEvent(Order order) {
        String channelName = getKey(ORDER, order.getInstrument());
        String value = toSizedOrderString(order);
        redisTemplate.convertAndSend(channelName, value);
    }

    private String toSignalString(Signal signal) {
        return String.format("%s,%d,%s",
            signal.getDirection(),
            Instant.from(signal.getTimestamp()).toEpochMilli(),
            signal.getPrice().toPlainString());
    }

    private String toSizedOrderString(Order order) {
        return String.format("%s,%d,%s,%d,%s,%s,%s",
            order.getInstrument(),
            order.getTimestamp().toInstant().toEpochMilli(),
            order.getDirection().name(),
            order.getQuantity(),
            valueOrEmpty(order.getPrice()),
            valueOrEmpty(order.getSignalStrength()),
            valueOrEmpty(order.getCapitalAllocated())
        );
    }

    private static String valueOrEmpty(BigDecimal val) {
        return val != null ? val.toPlainString() : "";
    }

    private Bar fromCsvString(String barEventValue, ZonedDateTime endTime) {

        if (null == barEventValue) {
            return new BaseBar(
                Duration.of(barConfiguration.getTimeFrame(), ChronoUnit.SECONDS),
                endTime,
                DecimalNum::valueOf);
        }

        String[] parts = barEventValue.split(",");
        return new BaseBar(
            Duration.of(barConfiguration.getTimeFrame(), ChronoUnit.SECONDS),
            endTime,
            Double.parseDouble(parts[0]), // open
            Double.parseDouble(parts[1]), // high
            Double.parseDouble(parts[2]), // low
            Double.parseDouble(parts[3]), // close
            Double.parseDouble(parts[4]), // volume
            Double.parseDouble(parts[5]), // amount
            Long.parseLong(parts[6]), // trades
            DecimalNum::valueOf
        );
    }

    private Bar getBar(String instrument, Object data, ZonedDateTime endTime) {
        log.debug("data for instrument {}, timestamp {} : {}", instrument, endTime, data);
        if (null == data) {
            return new BaseBar(
                Duration.of(barConfiguration.getTimeFrame(), ChronoUnit.SECONDS),
                endTime,
                DecimalNum::valueOf);
        }
        return fromCsvString(data.toString(), endTime);
    }

    private String getKey(String keySpace, String subKeySpace, String key) {
        return String.join(DELIMITER_DOT, keySpace, subKeySpace, key);
    }

    private String getKey(String keySpace, String key) {
        return String.join(DELIMITER_DOT, keySpace, key);
    }
}
