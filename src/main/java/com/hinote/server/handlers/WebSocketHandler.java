package com.hinote.server.handlers;

import com.hinote.server.models.ConnectedUser;
import com.hinote.server.services.RoomService;
import com.hinote.server.services.SynchronizationService;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.utils.IdGenerator;
import com.hinote.shared.utils.JsonUtil;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    
    private final RoomService roomService;
    private final SynchronizationService syncService;
    private final MessageRouter messageRouter;
    private final ConcurrentHashMap<WebSocket, ConnectedUser> connections;

    public WebSocketHandler(InetSocketAddress address, RoomService roomService, SynchronizationService syncService) {
        super(address);
        this.roomService = roomService;
        this.syncService = syncService;
        this.connections = new ConcurrentHashMap<>();
        this.messageRouter = new MessageRouter(roomService, syncService, this);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("New connection opened: {}", conn.getRemoteSocketAddress());
        // Create a temporary user until they join a room
        ConnectedUser user = new ConnectedUser(IdGenerator.generateUserId(), "Anonymous", conn);
        connections.put(conn, user);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("Connection closed: {} - Code: {}, Reason: {}", conn.getRemoteSocketAddress(), code, reason);
        ConnectedUser user = connections.remove(conn);
        if (user != null) {
            messageRouter.handleUserDisconnect(user.getUserId(), conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.debug("Received message: {}", message);
        try {
            Message msg = JsonUtil.fromJson(message, Message.class);
            if (msg != null) {
                messageRouter.routeMessage(msg, conn);
            } else {
                logger.error("Failed to parse message: {}", message);
            }
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("WebSocket error: {}", ex.getMessage(), ex);
    }

    @Override
    public void onStart() {
        logger.info("WebSocket server started successfully on {}", getAddress());
    }

    public void sendToClient(WebSocket conn, Message message) {
        if (conn != null && conn.isOpen()) {
            String jsonMessage = JsonUtil.toJson(message);
            if (jsonMessage != null) {
                conn.send(jsonMessage);
                logger.debug("Sent message to client: {}", message.getType());
            } else {
                logger.error("Failed to serialize message: {}", message);
            }
        }
    }

    public void broadcastToRoom(String roomId, Message message) {
        // Get all users in the room and send message to each
        roomService.getUsersInRoom(roomId).forEach(user -> {
            WebSocket conn = user.getConnection();
            if (conn != null && conn.isOpen()) {
                sendToClient(conn, message);
            }
        });
    }

    public ConnectedUser getUserFromConnection(WebSocket conn) {
        return connections.get(conn);
    }
}