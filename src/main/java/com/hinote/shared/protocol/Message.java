package com.hinote.shared.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class Message {
    @JsonProperty("type")
    private MessageType type;

    @JsonProperty("id")
    private String id;
    
    @JsonProperty("roomId")
    private String roomId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("payload")
    private JsonNode payload;

    public Message() {
        this.timestamp = LocalDateTime.now();
    }

    public Message(MessageType type, String id, String roomId, String userId, String username, JsonNode payload) {
        this();
        this.type = type;
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.payload = payload;
    }
    
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }
}
