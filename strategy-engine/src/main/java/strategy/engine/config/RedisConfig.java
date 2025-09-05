package strategy.engine.config;

import strategy.engine.component.Portfolio;
import strategy.engine.listener.BarDataHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import static strategy.engine.constant.ApplicationConstants.BAR;
import static strategy.engine.constant.ApplicationConstants.DELIMITER_DOT;
import static strategy.engine.constant.ApplicationConstants.NEW;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final Portfolio portfolio;

    @Bean
    public MessageListenerAdapter barDataMessageListenerAdapter(BarDataHandler barDataHandler) {
        return new MessageListenerAdapter(barDataHandler);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       MessageListenerAdapter barDataMessageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        for (String instrument: portfolio.getInstruments()) {
            container.addMessageListener(barDataMessageListenerAdapter,
                new ChannelTopic(String.join(DELIMITER_DOT, NEW, BAR, instrument)));
        }
        return container;
    }
}
