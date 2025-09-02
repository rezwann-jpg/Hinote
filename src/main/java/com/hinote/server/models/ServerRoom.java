package com.hinote.server.models;

import com.hinote.shared.protocol.DrawingOperationProtocol;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.protocol.TextOperationProtocol;
import com.hinote.shared.utils.JsonUtil;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.java_websocket.WebSocket;

public class ServerRoom {
    private final String roomId;
    private final String roomName;
    private final Set<ConnectedUser> users = ConcurrentHashMap.newKeySet();
    private final List<Message> chatHistory = new CopyOnWriteArrayList<>();
    private final List<Message> drawingHistory = new CopyOnWriteArrayList<>();
    private final List<Message> textHistory = new CopyOnWriteArrayList<>();

    public ServerRoom(String roomId) {
        this(roomId, "Room " + roomId);
    }

    public ServerRoom(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Set<ConnectedUser> getUsers() {
        return users;
    }

    public List<Message> getChatHistory() {
        return chatHistory;
    }

    public List<Message> getDrawingHistory() {
        return drawingHistory;
    }

    public List<Message> getTextHistory() {
        return textHistory;
    }

    public void addUser(ConnectedUser user) {
        users.add(user);
    }

    public void removeUser(String userId) {
        users.removeIf(user -> user.getUserId().equals(userId));
    }

    public void addChatMessage(Message message) {
        chatHistory.add(message);
    }

    public void addDrawingOperation(Message message) {
        drawingHistory.add(message);
    }

    public void addTextOperation(Message message) {
        textHistory.add(message);
    }

    public int getUserCount() {
        return users.size();
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }

    public void updateUserConnection(String userId, WebSocket conn) {
        users.stream()
             .filter(user -> user.getUserId().equals(userId))
             .findFirst()
             .ifPresent(user -> user.setConnection(conn));
    }

    public int removeOperationsByIds(List<String> operationIds) {
        int removedCount = 0;

        // Remove from drawing history
        removedCount += drawingHistory.removeIf(msg -> {
            try {
                DrawingOperationProtocol op = JsonUtil.fromJsonNode(
                    msg.getPayload(), 
                    DrawingOperationProtocol.class
                );
                return op != null && operationIds.contains(op.getOperationId());
            } catch (Exception e) {
                return false;
            }
        }) ? 1 : 0;

        // Remove from text history
        removedCount += textHistory.removeIf(msg -> {
            try {
                TextOperationProtocol op = JsonUtil.fromJsonNode(
                    msg.getPayload(), 
                    TextOperationProtocol.class
                );
                return op != null && operationIds.contains(op.getTextId());
            } catch (Exception e) {
                return false;
            }
        }) ? 1 : 0;

        return removedCount;
    }

    public void clearDrawingHistory() {
        drawingHistory.clear();
    }

    public void clearTextHistory() {
        textHistory.clear();
    }

    public boolean isOperationUndone(String operationId) {
        return false;
    }
}