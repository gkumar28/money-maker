package data.aggregator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import data.aggregator.service.RedisService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;

import java.time.Instant;
import java.util.Set;

import static data.aggregator.constant.ApplicationConstants.BAR;
import static data.aggregator.constant.ApplicationConstants.DATA;
import static data.aggregator.constant.ApplicationConstants.REDIS_MAX_BAR_COUNT;
import static data.aggregator.constant.ApplicationConstants.TIMESTAMP;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void raiseNewBarEvent(String instrument, Bar bar) {

        String dataKey = getKey(DATA, instrument);
        String timestamp = String.valueOf(bar.getEndTime().toInstant().toEpochMilli());
        String value = toCsvString(bar);
        String timestampKey = getKey(TIMESTAMP, instrument);

        redisTemplate.execute(new SessionCallback<Void>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> Void execute(RedisOperations<K, V> operations) throws DataAccessException {
                operations.multi();
                operations.opsForHash().put((K) dataKey, timestamp, value);
                operations.opsForZSet().add((K) timestampKey, (V) timestamp, -Instant.now().toEpochMilli());
                operations.exec();

                Set<V> oldTimestamps = operations.opsForZSet().range((K) timestampKey, REDIS_MAX_BAR_COUNT, -1);
                if (null != oldTimestamps && !oldTimestamps.isEmpty()) {
                    operations.opsForHash().delete((K) dataKey, oldTimestamps);
                    operations.opsForZSet().remove((K) timestampKey, oldTimestamps);
                }

                return null;
            }
        });


        String channel = String.format("new.bar.%s", instrument);
        log.debug("sending bar event for instrument {} : {} {}", instrument, timestamp, value);
        redisTemplate.convertAndSend(channel, timestamp);
    }

    private String toCsvString(Bar bar) {
        return String.format("%.5f,%.5f,%.5f,%.5f,%.2f,%.2f,%d",
            bar.getOpenPrice().doubleValue(),
            bar.getHighPrice().doubleValue(),
            bar.getLowPrice().doubleValue(),
            bar.getClosePrice().doubleValue(),
            bar.getVolume().doubleValue(),
            bar.getAmount().doubleValue(),
            bar.getTrades()
        );
    }

    private String getKey(String subKeySpace, String key) {
        return String.join(".", BAR, subKeySpace, key);
    }
}
