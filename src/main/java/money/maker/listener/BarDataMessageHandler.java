package money.maker.listener;

import lombok.RequiredArgsConstructor;
import money.maker.service.RedisService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class BarDataMessageHandler {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final RedisService redisService;

    public void handleMessage(Message message, String channel) {
        String timestamp = Arrays.toString(message.getBody());
        String instrument = extractInstrumentName(channel);
        redisService.onNewBarUpdate(instrument, timestamp);
    }

    private String extractInstrumentName(String channel) {
        String[] channelTokens = channel.split("\\.");
        return channelTokens[2];
    }
}
