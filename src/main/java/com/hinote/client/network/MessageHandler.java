package com.hinote.client.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hinote.client.models.ChatMessage;
import com.hinote.client.models.DrawingOperation;
import com.hinote.client.models.TextOperation;
import com.hinote.client.ui.MainController;
import com.hinote.shared.protocol.ChatMessageProtocol;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.protocol.MessageType;
import com.hinote.shared.protocol.TextOperationProtocol;
import com.hinote.shared.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final MainController mainController;

    public MessageHandler(MainController mainController) {
        this.mainController = mainController;
    }

    public Consumer<Message> handleMessage() {
        return this::handleMessageInternal;
    }

    private void handleMessageInternal(Message message) {
        if (message == null) {
            logger.warn("Received null message");
            return;
        }
        
        try {
            switch (message.getType()) {
                case CHAT_MESSAGE:
                    handleChatMessage(message);
                    break;

                case DRAW_OPERATION:
                    handleDrawingOperation(message);
                    break;

                case TEXT_OPERATION:
                    handleTextOperation(message);
                    break;

                case USER_JOINED:
                case USER_LEFT:
                    handleUserStatusChange(message);
                    break;

                case ROOM_JOINED:
                    handleRoomJoined(message);
                    break;

                case ROOM_LEFT:
                    handleRoomLeft(message);
                    break;

                case UNDO_OPERATION:
                    handleUndoOperation(message);
                    break;

                case REDO_OPERATION:
                    handleRedoOperation(message);
                    break;

                case CLEAR_OPERATION:
                    handleClearOperation(message);
                    break;

                case ERROR:
                    handleError(message);
                    break;

                case ACK:
                    handleAck(message);
                    break;

                default:
                    logger.warn("Unhandled message type: {}", message.getType());
            }
        } catch (Exception e) {
            logger.error("Error handling message of type {}: {}", message.getType(), e.getMessage(), e);
        }
    }

    private void handleChatMessage(Message message) {
        System.out.println("📩 CHAT_MESSAGE received: " + message.getPayload().toPrettyString());
        ChatMessageProtocol chatMsg = JsonUtil.fromJsonNode(message.getPayload(), ChatMessageProtocol.class);
        if (chatMsg != null && mainController != null) {
            mainController.addChatMessage(new ChatMessage(
                message.getUsername(),
                chatMsg.getContent(),
                chatMsg.isSystemMessage(),
                message.getTimestamp()
            ));
        }
    }

    private void handleDrawingOperation(Message message) {
        DrawingOperation drawOp = JsonUtil.fromJsonNode(message.getPayload(), DrawingOperation.class);
        if (drawOp != null && mainController != null) {
            mainController.applyDrawingOperation(drawOp);
        }
    }

    private void handleTextOperation(Message message) {
        TextOperationProtocol textOp = JsonUtil.fromJsonNode(message.getPayload(), TextOperationProtocol.class);
        if (textOp != null && mainController != null) {
            // Convert TextOperationProtocol to client-side TextOperation
            TextOperation textOperation = new TextOperation();
            
            // Convert enum type (if needed, Jackson should handle this automatically)
            if (textOp.getOperationType() != null) {
                textOperation.setOperationType(TextOperation.TextOperationType.valueOf(textOp.getOperationType().name()));
            }
            
            textOperation.setTextId(textOp.getTextId());
            textOperation.setX(textOp.getX());
            textOperation.setY(textOp.getY());
            textOperation.setContent(textOp.getContent());
            textOperation.setFontSize(textOp.getFontSize());
            textOperation.setFontFamily(textOp.getFontFamily());
            textOperation.setFontWeight(textOp.getFontWeight());
            textOperation.setFontStyle(textOp.getFontStyle());
            textOperation.setColor(textOp.getColor());
            textOperation.setRotation(textOp.getRotation());
            textOperation.setWidth(textOp.getWidth());
            textOperation.setHeight(textOp.getHeight());
            
            mainController.applyTextOperation(textOperation);
        }
    }

    private void handleUserStatusChange(Message message) {
        if (mainController != null) {
            String systemMessage = message.getPayload().asText();
            mainController.showSystemMessage(systemMessage);
            // Update participants list if needed
            if (message.getRoomId() != null) {
                mainController.updateParticipants(message.getRoomId());
            }
        }
    }

    private void handleRoomJoined(Message message) {
        if (mainController != null) {
            mainController.updateParticipants(message.getRoomId());
            mainController.showSystemMessage("You joined the room successfully");
        }
    }

    private void handleRoomLeft(Message message) {
        if (mainController != null) {
            mainController.updateParticipants(message.getRoomId());
            mainController.showSystemMessage("You left the room");
        }
    }

    private void handleError(Message message) {
        if (mainController != null) {
            String errorMessage = "Unknown error";
            try {
                if (message.getPayload().has("error")) {
                    errorMessage = message.getPayload().get("error").asText();
                } else {
                    errorMessage = message.getPayload().asText();
                }
            } catch (Exception e) {
                logger.warn("Could not parse error message payload");
            }
            mainController.showErrorMessage(errorMessage);
        }
    }

    // Updated handler methods for undo/redo with operation lists:
    private void handleUndoOperation(Message message) {
        if (mainController != null && !message.getUserId().equals(mainController.getUserId())) {
            try {
                List<DrawingOperation> operationsToRemove = JsonUtil.fromJson(
                    message.getPayload().toString(), 
                    new TypeReference<List<DrawingOperation>>() {}
                );
                mainController.performRemoteUndo(operationsToRemove);
            } catch (Exception e) {
                logger.error("Failed to parse undo operations: {}", e.getMessage(), e);
                mainController.showErrorMessage("Failed to process undo operation");
            }
        }
    }

    private void handleRedoOperation(Message message) {
        if (mainController != null && !message.getUserId().equals(mainController.getUserId())) {
            try {
                List<DrawingOperation> operationsToAdd = JsonUtil.fromJson(
                    message.getPayload().toString(), 
                    new TypeReference<List<DrawingOperation>>() {}
                );
                mainController.performRemoteRedo(operationsToAdd);
            } catch (Exception e) {
                logger.error("Failed to parse redo operations: {}", e.getMessage(), e);
                mainController.showErrorMessage("Failed to process redo operation");
            }
        }
    }

    private void handleClearOperation(Message message) {
        if (mainController != null && !message.getUserId().equals(mainController.getUserId())) {
            mainController.performRemoteClear();
        }
    }

    private void handleAck(Message message) {
        logger.debug("Received ACK: {}", message.getPayload().asText());
        // Handle heartbeat acknowledgment if needed
    }
}