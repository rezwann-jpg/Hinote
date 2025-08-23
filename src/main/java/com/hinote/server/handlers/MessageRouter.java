package com.hinote.server.handlers;

import com.hinote.server.models.ServerRoom;
import com.hinote.server.services.RoomService;
import com.hinote.server.services.SynchronizationService;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.protocol.MessageType;
import com.hinote.shared.utils.JsonUtil;
import com.hinote.shared.utils.IdGenerator;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageRouter {
    private static final Logger logger = LoggerFactory.getLogger(MessageRouter.class);
    private final RoomService roomService;
    private final SynchronizationService syncService;
    private WebSocketHandler webSocketHandler;

    public MessageRouter(RoomService roomService, SynchronizationService syncService, WebSocketHandler webSocketHandler) {
        this.roomService = roomService;
        this.syncService = syncService;
        this.webSocketHandler = webSocketHandler;
    }

    public void setWebSocketHandler(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public void routeMessage(Message message, WebSocket conn) {
        try {
            switch (message.getType()) {
                case JOIN_ROOM:
                    String roomId = message.getRoomId();
                    ServerRoom room = roomService.joinRoom(message.getUserId(), message.getUsername(), roomId);
                    if (room != null) {
                        webSocketHandler.sendToClient(conn, new Message(
                            MessageType.ROOM_JOINED,
                            IdGenerator.generateUniqueId(),
                            roomId,
                            message.getUserId(),
                            message.getUsername(),
                            JsonUtil.toJsonNode("Joined room successfully")
                        ));
                        webSocketHandler.broadcastToRoom(roomId, new Message(
                            MessageType.USER_JOINED,
                            IdGenerator.generateUniqueId(),
                            roomId,
                            message.getUserId(),
                            message.getUsername(),
                            JsonUtil.toJsonNode(message.getUsername() + " joined the room")
                        ));
                        syncService.sendRoomHistory(roomId, conn);
                    } else {
                        webSocketHandler.sendToClient(conn, new Message(
                            MessageType.ERROR,
                            IdGenerator.generateUniqueId(),
                            roomId,
                            message.getUserId(),
                            message.getUsername(),
                            JsonUtil.toJsonNode("Failed to join room")
                        ));
                    }
                    break;

                case CHAT_MESSAGE:
                    syncService.handleChatMessage(message);
                    webSocketHandler.broadcastToRoom(message.getRoomId(), message);
                    break;

                case DRAW_OPERATION:
                case TEXT_OPERATION:
                    syncService.handleOperation(message);
                    webSocketHandler.broadcastToRoom(message.getRoomId(), message);
                    break;

                case LEAVE_ROOM:
                    roomService.leaveRoom(message.getUserId(), message.getRoomId());
                    webSocketHandler.broadcastToRoom(message.getRoomId(), new Message(
                        MessageType.USER_LEFT,
                        IdGenerator.generateUniqueId(),
                        message.getRoomId(),
                        message.getUserId(),
                        message.getUsername(),
                        JsonUtil.toJsonNode(message.getUsername() + " left the room")
                    ));
                    break;

                case HEARTBEAT:
                    webSocketHandler.sendToClient(conn, new Message(
                        MessageType.ACK,
                        IdGenerator.generateUniqueId(),
                        message.getRoomId(),
                        message.getUserId(),
                        message.getUsername(),
                        JsonUtil.toJsonNode("Heartbeat acknowledged")
                    ));
                    break;

                default:
                    logger.warn("Unhandled message type: {}", message.getType());
            }
        } catch (Exception e) {
            logger.error("Error routing message: {}", e.getMessage(), e);
            webSocketHandler.sendToClient(conn, new Message(
                MessageType.ERROR,
                IdGenerator.generateUniqueId(),
                message.getRoomId(),
                message.getUserId(),
                message.getUsername(),
                JsonUtil.toJsonNode("Server error: " + e.getMessage())
            ));
        }
    }

    public void handleUserDisconnect(String userId, WebSocket conn) {
        String roomId = roomService.getUserRoom(userId);
        if (roomId != null) {
            roomService.leaveRoom(userId, roomId);
            webSocketHandler.broadcastToRoom(roomId, new Message(
                MessageType.USER_LEFT,
                IdGenerator.generateUniqueId(),
                roomId,
                userId,
                null,
                JsonUtil.toJsonNode("User disconnected")
            ));
        }
    }

    public boolean isUserInRoom(String userId, String roomId) {
        return roomService.isUserInRoom(userId, roomId);
    }
}
