package com.hinote.client.network;

import com.hinote.shared.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private final URI serverUri;
    private WebSocketClientImpl client;
    private final Consumer<Message> messageHandler;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private boolean isReconnecting = false;

    public ConnectionManager(String serverUrl, Consumer<Message> messageHandler) {
        this.serverUri = URI.create(serverUrl);
        this.messageHandler = messageHandler;
        connect();
    }

    public void connect() {
        client = new WebSocketClient(serverUri, messageHandler);
        try {
            client.connect();
            logger.info("Initiating connection to {}", serverUri);
        } catch (Exception e) {
            logger.error("Failed to connect: {}", e.getMessage(), e);
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (!isReconnecting) {
            isReconnecting = true;
            executor.schedule(this::attemptReconnect, 5, TimeUnit.SECONDS);
        }
    }

    private void attemptReconnect() {
        logger.info("Attempting to reconnect...");
        try {
            client.reconnect();
            isReconnecting = false;
        } catch (Exception e) {
            logger.error("Reconnect failed: {}", e.getMessage(), e);
            scheduleReconnect();
        }
    }

    public void sendMessage(Message message) {
        if (client != null && client.isOpen()) {
            client.sendMessage(message);
        } else {
            logger.warn("Cannot send message: WebSocket is not connected");
        }
    }

    public void disconnect() {
        if (client != null) {
            client.close();
        }
        executor.shutdown();
    }

    public boolean isConnected() {
        return client != null && client.isOpen();
    }
}