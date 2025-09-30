package strategy.engine.config;

import strategy.engine.listener.BarDataHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import strategy.engine.service.PortfolioManagementService;

import static strategy.engine.constant.ApplicationConstants.BAR;
import static strategy.engine.constant.ApplicationConstants.DELIMITER_DOT;
import static strategy.engine.constant.ApplicationConstants.NEW;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final PortfolioManagementService portfolioManagementService;

    @Bean
    public MessageListenerAdapter barDataMessageListenerAdapter(BarDataHandler barDataHandler) {
        return new MessageListenerAdapter(barDataHandler);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       MessageListenerAdapter barDataMessageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        for (String instrument: portfolioManagementService.getInstruments()) {
            container.addMessageListener(barDataMessageListenerAdapter,
                new ChannelTopic(String.join(DELIMITER_DOT, NEW, BAR, instrument)));
        }
        return container;
    }
}
