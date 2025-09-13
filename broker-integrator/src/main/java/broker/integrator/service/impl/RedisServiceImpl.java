package broker.integrator.service.impl;

import broker.integrator.schemaobject.BrokerContext;
import broker.integrator.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static broker.integrator.constant.ApplicationConstant.BROKER;
import static broker.integrator.constant.ApplicationConstant.CONNECTED;
import static broker.integrator.constant.ApplicationConstant.DELIMITER_DOT;
import static broker.integrator.constant.ApplicationConstant.LIFECYCLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void raiseConnectedEvent(String broker, String accessToken) {
        BrokerContext context = new BrokerContext(broker, CONNECTED, accessToken);
        redisTemplate.convertAndSend(String.join(DELIMITER_DOT, BROKER, LIFECYCLE), toCsvString(context));
    }

    private String toCsvString(BrokerContext brokerContext) {
        return String.format("%s,%s,%s", brokerContext.getClient(), brokerContext.getEvent(), brokerContext.getAccessToken());
    }
}
