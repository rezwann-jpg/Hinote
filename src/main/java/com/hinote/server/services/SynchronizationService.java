package com.hinote.server.services;

import com.hinote.server.models.ServerRoom;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.protocol.MessageType;
import com.hinote.shared.utils.JsonUtil;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SynchronizationService {
    private static final Logger logger = LoggerFactory.getLogger(SynchronizationService.class);
    private final ConcurrentHashMap<String, ServerRoom> rooms;

    public SynchronizationService(ConcurrentHashMap<String, ServerRoom> rooms) {
        this.rooms = rooms;
    }

    public void handleChatMessage(Message message) {
        logger.info("Chat message received from user {}: {}", message.getUsername(), 
                   message.getPayload().toString());
        
        // Store message in room history
        ServerRoom room = rooms.get(message.getRoomId());
        if (room != null) {
            room.addChatMessage(message);
        }
    }

    public void handleOperation(Message message) {
        logger.info("Operation received: {} from user {}", message.getType(), message.getUsername());
        
        ServerRoom room = rooms.get(message.getRoomId());
        if (room != null) {
            // Store operation in room history based on type
            switch (message.getType()) {
                case DRAW_OPERATION:
                    room.addDrawingOperation(message);
                    break;
                case TEXT_OPERATION:
                    room.addTextOperation(message);
                    break;
            }
        }
    }

    public void sendRoomHistory(String roomId, WebSocket conn) {
        ServerRoom room = rooms.get(roomId);
        if (room == null) {
            logger.warn("Room not found: {}", roomId);
            return;
        }

        logger.info("Sending room history for room: {}", roomId);

        // Send chat history
        List<Message> chatHistory = room.getChatHistory();
        for (Message chatMsg : chatHistory) {
            sendMessageToClient(conn, chatMsg);
        }

        // Send drawing history
        List<Message> drawingHistory = room.getDrawingHistory();
        for (Message drawOp : drawingHistory) {
            sendMessageToClient(conn, drawOp);
        }

        // Send text history
        List<Message> textHistory = room.getTextHistory();
        for (Message textOp : textHistory) {
            sendMessageToClient(conn, textOp);
        }

        logger.info("Sent {} chat, {} drawing, and {} text operations as history",
                   chatHistory.size(), drawingHistory.size(), textHistory.size());
    }

    private void sendMessageToClient(WebSocket conn, Message message) {
        if (conn != null && conn.isOpen()) {
            String jsonMessage = JsonUtil.toJson(message);
            if (jsonMessage != null) {
                conn.send(jsonMessage);
                logger.debug("Sent history message to client: {}", message.getType());
            } else {
                logger.error("Failed to serialize history message: {}", message);
            }
        }
    }
}