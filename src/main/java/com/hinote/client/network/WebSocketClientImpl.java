package com.hinote.client.network;

import com.hinote.shared.protocol.Message;
import com.hinote.shared.utils.JsonUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.function.Consumer;

public class WebSocketClientImpl extends WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
    private final Consumer<Message> messageHandler;

    public WebSocketClientImpl(URI serverUri, Consumer<Message> messageHandler) {
        super(serverUri);
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Connected to server: {}", getURI());
    }

    @Override
    public void onMessage(String message) {
        logger.debug("Received message: {}", message);
        Message msg = JsonUtil.fromJson(message, Message.class);
        if (msg != null) {
            messageHandler.accept(msg);
        } else {
            logger.error("Failed to parse message: {}", message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Connection closed: code={}, reason={}, remote={}", code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.error("WebSocket error: {}", ex.getMessage(), ex);
    }

    public void sendMessage(Message message) {
        String json = JsonUtil.toJson(message);
        if (json != null) {
            send(json);
            logger.debug("Sent message: {}", json);
        } else {
            logger.error("Failed to serialize message: {}", message);
        }
    }
}