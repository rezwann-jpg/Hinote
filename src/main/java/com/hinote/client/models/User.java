package com.hinote.client.models;

import java.time.LocalDateTime;

public class User {
    private String userId;
    private String username;
    private LocalDateTime joinTime;
    private boolean isOnline;

    public User() {
        this.joinTime = LocalDateTime.now();
        this.isOnline = true;
    }

    public User(String userId, String username) {
        this();
        this.userId = userId;
        this.username = username;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId != null ? userId.equals(user.userId) : user.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", joinTime=" + joinTime +
                ", isOnline=" + isOnline +
                '}';
    }
}