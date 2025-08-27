package com.hinote.server.services;

import com.hinote.server.models.ConnectedUser;
import com.hinote.server.models.ServerRoom;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.utils.IdGenerator;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final ConcurrentHashMap<String, ServerRoom> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userRoomMapping = new ConcurrentHashMap<>();

    public ServerRoom joinRoom(String userId, String username, String roomId) {
        try {
            // Create room if it doesn't exist
            ServerRoom room = rooms.computeIfAbsent(roomId, ServerRoom::new);
            
            ConnectedUser user = new ConnectedUser(userId, username, null); // null connection for now
            
            // Add user to room
            room.addUser(user);
            userRoomMapping.put(userId, roomId);
            
            logger.info("User {} joined room {}", username, roomId);
            return room;
        } catch (Exception e) {
            logger.error("Error joining room: {}", e.getMessage(), e);
            return null;
        }
    }

    public void leaveRoom(String userId, String roomId) {
        try {
            ServerRoom room = rooms.get(roomId);
            if (room != null) {
                room.removeUser(userId);
                userRoomMapping.remove(userId);
                
                // Clean up empty rooms
                if (room.isEmpty()) {
                    rooms.remove(roomId);
                }
                
                logger.info("User {} left room {}", userId, roomId);
            }
        } catch (Exception e) {
            logger.error("Error leaving room: {}", e.getMessage(), e);
        }
    }

    public String getUserRoom(String userId) {
        return userRoomMapping.get(userId);
    }

    public boolean isUserInRoom(String userId, String roomId) {
        ServerRoom room = rooms.get(roomId);
        if (room != null) {
            return room.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(userId));
        }
        return false;
    }

    public Set<ConnectedUser> getUsersInRoom(String roomId) {
        ServerRoom room = rooms.get(roomId);
        if (room != null) {
            return room.getUsers();
        }
        return Collections.emptySet();
    }

    // Additional utility methods
    public ServerRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public int getRoomUserCount(String roomId) {
        ServerRoom room = rooms.get(roomId);
        return room != null ? room.getUserCount() : 0;
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    public void updateUserConnection(String userId, String roomId, WebSocket conn) {
        ServerRoom room = rooms.get(roomId);
        if (room != null) {
            room.updateUserConnection(userId, conn);
            logger.info("Updated connection for user {} in room {}", userId, roomId);
        }
    }
}