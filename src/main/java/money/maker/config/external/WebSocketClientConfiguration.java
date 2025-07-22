package money.maker.config.external;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "websocket.configuration")
public class WebSocketClientConfiguration {

    private final String uri;
    private final long reconnectTimeout;

}
