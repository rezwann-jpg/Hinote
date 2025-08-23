package com.hinote.client.models;

import java.time.LocalDateTime;

public class ChatMessage {
    private String username;
    private String content;
    private boolean isSystemMessage;
    private LocalDateTime timestamp;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String username, String content, boolean isSystemMessage, LocalDateTime timestamp) {
        this.username = username;
        this.content = content;
        this.isSystemMessage = isSystemMessage;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isSystemMessage() { return isSystemMessage; }
    public void setSystemMessage(boolean systemMessage) { isSystemMessage = systemMessage; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "username='" + username + '\'' +
                ", content='" + content + '\'' +
                ", isSystemMessage=" + isSystemMessage +
                ", timestamp=" + timestamp +
                '}';
    }
}