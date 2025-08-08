package money.maker.config;

import lombok.RequiredArgsConstructor;
import money.maker.component.Portfolio;
import money.maker.listener.BarDataMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import static money.maker.constant.ApplicationConstants.BAR;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final Portfolio portfolio;

    @Bean
    public MessageListenerAdapter barDataMessageListenerAdapter(BarDataMessageHandler barDataMessageHandler) {
        return new MessageListenerAdapter(barDataMessageHandler);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       MessageListenerAdapter barDataMessageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        for (String instrument: portfolio.getInstruments()) {
            container.addMessageListener(barDataMessageListenerAdapter,
                new ChannelTopic(String.join(".", "new", BAR, instrument)));
        }
        return container;
    }
}
