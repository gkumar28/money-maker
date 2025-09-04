package data.emitter.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import data.emitter.service.RedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static data.emitter.constant.ApplicationConstants.DELIMITER_DOT;
import static data.emitter.constant.ApplicationConstants.PRICE;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void raiseTickEvent(String instrument, String message) {
        log.debug("sending tick event for instrument {} : {}", instrument, message);
        String channel = String.format("new.tick.%s", instrument);
        redisTemplate.convertAndSend(channel, message);
    }

    @Override
    public void updateInstrumentPrice(String instrument, double price) {
        redisTemplate.opsForValue().set(getKey(PRICE, instrument), String.format("%.2f", price));
    }

    private String getKey(String keySpace, String key) {
        return String.join(DELIMITER_DOT, keySpace, key);
    }
}
