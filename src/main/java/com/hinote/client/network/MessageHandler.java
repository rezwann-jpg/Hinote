package com.hinote.client.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hinote.client.models.ChatMessage;
import com.hinote.client.models.DrawingOperation;
import com.hinote.client.models.TextOperation;
import com.hinote.client.ui.MainController;
import com.hinote.shared.protocol.BatchDrawingOperationProtocol;
import com.hinote.shared.protocol.ChatMessageProtocol;
import com.hinote.shared.protocol.DrawingOperationProtocol;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.protocol.MessageType;
import com.hinote.shared.protocol.TextOperationProtocol;
import com.hinote.shared.protocol.UndoOperationProtocol;
import com.hinote.shared.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

                case BATCH_DRAW_OPERATION:
                    handleBatchDrawOperation(message);
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

    private void handleBatchDrawOperation(Message message) {
        if (!message.getUserId().equals(mainController.getUserId())) {
            BatchDrawingOperationProtocol batch = JsonUtil.fromJsonNode(
                message.getPayload(), 
                BatchDrawingOperationProtocol.class
            );
            
            List<DrawingOperation> operations = new ArrayList<>();
            for (DrawingOperationProtocol protocol : batch.getOperations()) {
                DrawingOperation op = new DrawingOperation();
                op.setOperationType(protocol.getOperationType());
                op.setStartX(protocol.getStartX());
                op.setStartY(protocol.getStartY());
                op.setEndX(protocol.getEndX());
                op.setEndY(protocol.getEndY());
                op.setColor(protocol.getColor());
                op.setStrokeWidth(protocol.getStrokeWidth());
                op.setUserId(message.getUserId());
                op.setOperationId(protocol.getOperationId());
                operations.add(op);
            }
            
            mainController.applyBatchOperations(operations);
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
        if (!message.getUserId().equals(mainController.getUserId())) {
            DrawingOperationProtocol protocol = JsonUtil.fromJsonNode(
                message.getPayload(), 
                DrawingOperationProtocol.class
            );

            DrawingOperation operation = new DrawingOperation();
            operation.setOperationType(protocol.getOperationType());
            operation.setStartX(protocol.getStartX());
            operation.setStartY(protocol.getStartY());
            operation.setEndX(protocol.getEndX());
            operation.setEndY(protocol.getEndY());
            operation.setColor(protocol.getColor());
            operation.setStrokeWidth(protocol.getStrokeWidth());
            operation.setUserId(message.getUserId());
            operation.setOperationId(protocol.getOperationId()); // Add this line

            mainController.applyDrawingOperation(operation);
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

    private void handleUndoOperation(Message message) {
        if (!message.getUserId().equals(mainController.getUserId())) {
            UndoOperationProtocol undoProtocol = JsonUtil.fromJsonNode(
                message.getPayload(), 
                UndoOperationProtocol.class
            );
            
            // Create dummy operations with just the IDs for removal
            List<DrawingOperation> operationsToRemove = new ArrayList<>();
            for (String opId : undoProtocol.getOperationIds()) {
                DrawingOperation op = new DrawingOperation();
                op.setOperationId(opId);
                operationsToRemove.add(op);
            }
            
            mainController.performRemoteUndo(operationsToRemove);
        }
    }

    private void handleRedoOperation(Message message) {
        if (!message.getUserId().equals(mainController.getUserId())) {
            // Redo is handled as a batch operation
            handleBatchDrawOperation(message);
        }
    }

    private void handleClearOperation(Message message) {
        if (!message.getUserId().equals(mainController.getUserId())) {
            mainController.performRemoteClear();
        }
    }

    private void handleAck(Message message) {
        logger.debug("Received ACK: {}", message.getPayload().asText());
        // Handle heartbeat acknowledgment if needed
    }
}