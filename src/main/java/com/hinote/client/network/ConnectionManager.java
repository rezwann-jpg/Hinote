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
        try {
            client = new WebSocketClientImpl(serverUri, messageHandler);
            client.connectBlocking(); // Use blocking connect for better control
            if (client.isOpen()) {
                logger.info("Successfully connected to {}", serverUri);
            } else {
                logger.error("Failed to connect to {}", serverUri);
                scheduleReconnect();
            }
        } catch (Exception e) {
            logger.error("Failed to connect: {}", e.getMessage(), e);
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (!isReconnecting) {
            isReconnecting = true;
            logger.info("Scheduling reconnection in 5 seconds...");
            executor.schedule(this::attemptReconnect, 5, TimeUnit.SECONDS);
        }
    }

    private void attemptReconnect() {
        logger.info("Attempting to reconnect...");
        try {
            if (client != null) {
                client.close();
            }
            client = new WebSocketClientImpl(serverUri, messageHandler);
            client.connectBlocking(5, TimeUnit.SECONDS); // 5 second timeout
            
            if (client.isOpen()) {
                logger.info("Reconnection successful");
                isReconnecting = false;
            } else {
                logger.error("Reconnection failed");
                scheduleReconnect();
            }
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
            try {
                client.closeBlocking();
            } catch (InterruptedException e) {
                logger.error("Interrupted while closing connection: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public boolean isConnected() {
        return client != null && client.isOpen();
    }
    
    public boolean isConnecting() {
        return client != null && client.isConnecting();
    }
}