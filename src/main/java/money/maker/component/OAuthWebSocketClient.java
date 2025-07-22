package money.maker.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import money.maker.config.external.WebSocketClientConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Setter
@Slf4j
@Component
public class OAuthWebSocketClient extends WebSocketClient {

    private final WebSocketClientConfiguration webSocketClientConfiguration;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final WebSocketDataDispatcher webSocketDataDispatcher;

    private Set<String> instruments = new HashSet<>();
    private AtomicBoolean toReconnect = new AtomicBoolean(false);
    private final AtomicReference<ScheduledFuture<?>> reconnectionFuture = new AtomicReference<>();

    // Inject Camel's ProducerTemplate to push messages into routes
    public OAuthWebSocketClient(WebSocketClientConfiguration webSocketClientConfiguration,
                                ThreadPoolTaskScheduler threadPoolTaskScheduler,
                                WebSocketDataDispatcher webSocketDataDispatcher)
        throws URISyntaxException {
        super(new URI(webSocketClientConfiguration.getUri()));

        this.webSocketDataDispatcher = webSocketDataDispatcher;
        this.webSocketClientConfiguration = webSocketClientConfiguration;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    @Override
    public void connect() {
        if (super.isOpen()) {
            return;
        }
        log.info("Connecting to WebSocket URI: {}", webSocketClientConfiguration.getUri());
        super.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("Connected to WebSocket URI: {}", webSocketClientConfiguration.getUri());
        // prevent future reconnection attempts, if any setup
        toReconnect.set(false);
        // Send auth JSON after connection
        String authMessage = "{\"action\":\"subscribe\",\"tokens\":\"<your_token_here>\"}";
        send(authMessage);
    }

    @Override
    public void onMessage(String message) {
        log.info("Received message: {}", message);
        try {
            webSocketDataDispatcher.sendMessage(message);
        } catch (JsonProcessingException e) {
            log.error("could not process message: {}", e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket closed. URI: {}, Code: {}, Reason: {}, ", webSocketClientConfiguration.getUri(), code, reason);

        if (CloseFrame.NORMAL != code &&
            toReconnect.compareAndSet(false, true)) {
            reconnectWithBackoff();
        }
    }

    @Override
    public void close() {
        if (!this.isOpen()) {
            return;
        }
        log.info("Gracefully closing WebSocket URI: {}", webSocketClientConfiguration.getUri());
        super.close(CloseFrame.NORMAL);
    }

    @Override
    public void onError(Exception ex) {
        log.error("Error received in WebSocket URI: {}. error: ", webSocketClientConfiguration.getUri(), ex);
    }

    @PreDestroy
    public void cleanup() {
        if (this.isOpen()) {
            log.info("Closing WebSocket client connection gracefully");
            this.close();
        }
    }

    // Example reconnect with 1s backoff
    private void reconnectWithBackoff() {

        ScheduledFuture<?> scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(() -> {
            if (toReconnect.get()) {
                log.info("Attempting to reconnect...");
                super.reconnect();
            } else {
                reconnectionFuture.get().cancel(false);
            }
        }, Duration.of(webSocketClientConfiguration.getReconnectTimeout(), ChronoUnit.MILLIS));

        reconnectionFuture.set(scheduledFuture);
    }
}
