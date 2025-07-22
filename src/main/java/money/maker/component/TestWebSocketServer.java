package money.maker.component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ConditionalOnProperty(name = "testing.enable", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class TestWebSocketServer {

    // Server port can be configured externally
    @Value("${testing.websocket.server.port:8081}")
    private int port;

    private EmbeddedWebSocketServer server;

    @PostConstruct
    public void start() {
        log.debug("Starting mock WebSocket server on port {}", port);
        server = new EmbeddedWebSocketServer(new InetSocketAddress(port));
        server.start();

        log.debug("Mock WebSocket server started");
    }

    @PreDestroy
    public void cleanup() {
        if (server != null) {
            log.debug("Stopping WebSocket server gracefully");
            try {
                server.stop(1000); // timeout in ms, adjust as needed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while stopping WebSocket server", e);
            }
        }
    }

    public void broadcastTick(String json) {
        if (server != null) {
            server.broadcast(json);
        }
    }

    // Embedded WebSocketServer implementation
    private static class EmbeddedWebSocketServer extends WebSocketServer {

        private final Set<WebSocket> connections = new CopyOnWriteArraySet<>();

        public EmbeddedWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            connections.add(conn);
            log.info("Client connected: {}", conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            connections.remove(conn);
            log.info("Client disconnected: {} reason: {}", conn.getRemoteSocketAddress(), reason);
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            log.info("Received message from client: {}", message);
            // Optionally handle client messages
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            log.error("WebSocket error", ex);
        }

        @Override
        public void onStart() {
            log.info("WebSocket server started");
        }

        @Override
        public void broadcast(String message) {
            connections.forEach(conn -> {
                if (conn.isOpen()) {
                    conn.send(message);
                }
            });
            log.debug("Broadcasted message to {} clients: {}", connections.size(), message);
        }
    }
}
