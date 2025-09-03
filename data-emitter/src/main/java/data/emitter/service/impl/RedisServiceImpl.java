package data.emitter.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import data.emitter.service.RedisService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
}
