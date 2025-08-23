package com.hinote.client.network;

import com.hinote.shared.protocol.Message;
import com.hinote.shared.utils.JsonUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class WebSocketClientImpl extends WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientImpl.class);
    private final Consumer<Message> messageHandler;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);

    public WebSocketClientImpl(URI serverUri, Consumer<Message> messageHandler) {
        super(serverUri);
        this.messageHandler = messageHandler;
        // Set connection timeout
        setConnectionLostTimeout(30);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        isConnected.set(true);
        isConnecting.set(false);
        logger.info("Connected to server: {}", getURI());
    }

    @Override
    public void onMessage(String message) {
        logger.debug("Received message: {}", message);
        try {
            Message msg = JsonUtil.fromJson(message, Message.class);
            if (msg != null) {
                messageHandler.accept(msg);
            } else {
                logger.error("Failed to parse message: {}", message);
            }
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        isConnected.set(false);
        isConnecting.set(false);
        logger.info("Connection closed: code={}, reason={}, remote={}", code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        isConnecting.set(false);
        logger.error("WebSocket error: {}", ex.getMessage(), ex);
    }

    public void sendMessage(Message message) {
        if (isOpen()) {
            String json = JsonUtil.toJson(message);
            if (json != null) {
                send(json);
                logger.debug("Sent message: {}", json);
            } else {
                logger.error("Failed to serialize message: {}", message);
            }
        } else {
            logger.warn("Cannot send message: WebSocket is not open");
        }
    }

    // Custom method to track connecting state
    public boolean isConnecting() {
        return isConnecting.get();
    }

    // Override isOpen to also check our internal state
    @Override
    public boolean isOpen() {
        return super.isOpen() && isConnected.get();
    }

    // Override connect methods to track connecting state
    @Override
    public void connect() {
        isConnecting.set(true);
        super.connect();
    }

    @Override
    public boolean connectBlocking() throws InterruptedException {
        isConnecting.set(true);
        boolean result = super.connectBlocking();
        isConnecting.set(false);
        return result;
    }

    @Override
    public boolean connectBlocking(long timeout, java.util.concurrent.TimeUnit timeUnit) throws InterruptedException {
        isConnecting.set(true);
        boolean result = super.connectBlocking(timeout, timeUnit);
        isConnecting.set(false);
        return result;
    }
}