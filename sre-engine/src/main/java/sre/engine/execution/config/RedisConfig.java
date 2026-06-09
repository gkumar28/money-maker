package sre.engine.execution.config;

import sre.engine.execution.listener.SignalGenerationHandler;
import sre.engine.execution.listener.TickDataHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import sre.engine.execution.component.Portfolio;

import static sre.engine.execution.constant.ApplicationConstants.DELIMITER_DOT;
import static sre.engine.execution.constant.ApplicationConstants.SIGNAL;
import static sre.engine.execution.constant.ApplicationConstants.TICK;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final Portfolio portfolio;

    @Bean("tickDataMessageListenerAdapter")
    public MessageListenerAdapter tickDataMessageListenerAdapter(TickDataHandler tickDataHandler) {
        return new MessageListenerAdapter(tickDataHandler);
    }

    @Bean("signalGenerationListenerAdapter")
    public MessageListenerAdapter signalGenerationListenerAdapter(SignalGenerationHandler signalGenerationHandler) {
        return new MessageListenerAdapter(signalGenerationHandler);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       MessageListenerAdapter tickDataMessageListenerAdapter,
                                                                       MessageListenerAdapter signalGenerationListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        for (String instrument: portfolio.getInstruments()) {

            container.addMessageListener(tickDataMessageListenerAdapter,
                new ChannelTopic(String.join(DELIMITER_DOT, TICK, instrument)));
            container.addMessageListener(signalGenerationListenerAdapter,
                new ChannelTopic(String.join(DELIMITER_DOT, SIGNAL, instrument)));

        }
        return container;
    }
}
