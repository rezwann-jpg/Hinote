package com.hinote.client.network;

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

public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final MainController mainController;

    public MessageHandler(MainController mainController) {
        this.mainController = mainController;
    }

    public void handleMessage(Message message) {
        switch (message.getType()) {
            case CHAT_MESSAGE:
                ChatMessageProtocol chatMsg = JsonUtil.fromJsonNode(message.getPayload(), ChatMessageProtocol.class);
                if (chatMsg != null) {
                    mainController.addChatMessage(new ChatMessage(
                        message.getUsername(),
                        chatMsg.getContent(),
                        chatMsg.isSystemMessage(),
                        message.getTimestamp()
                    ));
                }
                break;

            case DRAW_OPERATION:
                DrawingOperation drawOp = JsonUtil.fromJsonNode(message.getPayload(), DrawingOperation.class);
                if (drawOp != null) {
                    mainController.applyDrawingOperation(drawOp);
                }
                break;

            case TEXT_OPERATION:
                TextOperationProtocol textOp = JsonUtil.fromJsonNode(message.getPayload(), TextOperationProtocol.class);
                if (textOp != null) {
                    mainController.applyTextOperation(new TextOperation(
                        textOp.getTextId(),
                        textOp.getOperationType(),
                        textOp.getX(),
                        textOp.getY(),
                        textOp.getContent(),
                        textOp.getFontSize(),
                        textOp.getFontFamily(),
                        textOp.getFontWeight(),
                        textOp.getFontStyle(),
                        textOp.getColor(),
                        textOp.getRotation()
                    ));
                }
                break;

            case ROOM_JOINED:
                mainController.updateParticipants(message.getRoomId());
                mainController.showSystemMessage(String.format("%s joined the room", message.getUsername()));
                break;

            case ROOM_LEFT:
                mainController.updateParticipants(message.getRoomId());
                mainController.showSystemMessage(String.format("%s left the room", message.getUsername()));
                break;

            case ERROR:
                mainController.showErrorMessage(message.getPayload().get("error").asText());
                break;

            default:
                logger.warn("Unhandled message type: {}", message.getType());
        }
    }
}