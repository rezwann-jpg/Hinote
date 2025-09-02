package com.hinote.server.services;

import com.hinote.server.models.ServerRoom;
import com.hinote.shared.protocol.*;
import com.hinote.shared.utils.JsonUtil;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    public void handleBatchOperation(Message message) {
        logger.info("Batch operation received from user {}", message.getUsername());
        
        ServerRoom room = rooms.get(message.getRoomId());
        if (room != null) {
            // Parse the batch and add individual operations to history
            try {
                BatchDrawingOperationProtocol batch = JsonUtil.fromJsonNode(
                    message.getPayload(), 
                    BatchDrawingOperationProtocol.class
                );
                
                if (batch != null && batch.getOperations() != null) {
                    for (DrawingOperationProtocol op : batch.getOperations()) {
                        // Create individual message for each operation in the batch
                        Message drawMessage = new Message(
                            MessageType.DRAW_OPERATION,
                            message.getId() + "-" + op.getOperationId(),
                            message.getRoomId(),
                            message.getUserId(),
                            message.getUsername(),
                            JsonUtil.toJsonNode(op)
                        );
                        room.addDrawingOperation(drawMessage);
                    }
                    logger.info("Added {} operations from batch to room history", 
                               batch.getOperations().size());
                }
            } catch (Exception e) {
                logger.error("Error processing batch operation: {}", e.getMessage(), e);
            }
        }
    }

    public void handleUndoOperation(Message message) {
        logger.info("Undo operation received from user {} in room {}", 
                   message.getUsername(), message.getRoomId());
        
        ServerRoom room = rooms.get(message.getRoomId());
        if (room != null) {
            try {
                UndoOperationProtocol undoProtocol = JsonUtil.fromJsonNode(
                    message.getPayload(), 
                    UndoOperationProtocol.class
                );
                
                if (undoProtocol != null && undoProtocol.getOperationIds() != null) {
                    // Remove the specified operations from room history
                    List<String> operationIds = undoProtocol.getOperationIds();
                    int removedCount = room.removeOperationsByIds(operationIds);
                    logger.info("Removed {} operations from room history for undo", removedCount);
                }
            } catch (Exception e) {
                logger.error("Error processing undo operation: {}", e.getMessage(), e);
            }
        }
    }

    public void handleRedoOperation(Message message) {
        logger.info("Redo operation received from user {} in room {}", 
                   message.getUsername(), message.getRoomId());
        
        // Redo is typically handled as a batch operation on the client side
        // The client sends the operations to redo as a batch
        // So we can treat it similarly to a batch operation
        handleBatchOperation(message);
    }

    public void handleClearOperation(Message message) {
        logger.info("Clear operation received from user {} in room {}", 
                   message.getUsername(), message.getRoomId());
        
        ServerRoom room = rooms.get(message.getRoomId());
        if (room != null) {
            // Clear all drawing and text operations from the room
            room.clearDrawingHistory();
            room.clearTextHistory();
            logger.info("Cleared canvas history for room: {}", message.getRoomId());
            
            // Optionally, you might want to add a clear operation to history
            // so new users know the canvas was cleared
            room.addDrawingOperation(message);
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
            // Skip sending individual operations that were part of an undone batch
            if (!room.isOperationUndone(drawOp.getId())) {
                sendMessageToClient(conn, drawOp);
            }
        }

        // Send text history
        List<Message> textHistory = room.getTextHistory();
        for (Message textOp : textHistory) {
            if (!room.isOperationUndone(textOp.getId())) {
                sendMessageToClient(conn, textOp);
            }
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