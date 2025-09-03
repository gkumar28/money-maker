package data.aggregator.config;

import lombok.RequiredArgsConstructor;
import data.aggregator.component.Portfolio;
import data.aggregator.listener.TickDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import static data.aggregator.constant.ApplicationConstants.DELIMITER_DOT;
import static data.aggregator.constant.ApplicationConstants.NEW;
import static data.aggregator.constant.ApplicationConstants.TICK;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    private final Portfolio portfolio;

    @Bean
    public MessageListenerAdapter tickMessageListenerAdapter(TickDataHandler tickDataHandler) {
        MessageListenerAdapter tickMessageListenerAdapter = new MessageListenerAdapter(tickDataHandler);
        tickMessageListenerAdapter.afterPropertiesSet();
        return tickMessageListenerAdapter;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       MessageListenerAdapter tickMessageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        for (String instrument: portfolio.getInstruments()) {
            String channelName = String.join(DELIMITER_DOT, NEW, TICK, instrument);
            container.addMessageListener(tickMessageListenerAdapter,
                new ChannelTopic(channelName));
            log.debug("subscribed to channel: {}", channelName);
        }
        return container;
    }
}
