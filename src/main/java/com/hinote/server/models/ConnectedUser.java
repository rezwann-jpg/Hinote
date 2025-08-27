package com.hinote.server.models;

import org.java_websocket.WebSocket;

import java.time.LocalDateTime;
import java.util.Objects;

public class ConnectedUser {
    private final String userId;
    private final String username;
    private WebSocket connection;
    private final LocalDateTime joinTime;

    public ConnectedUser(String userId, String username, WebSocket connection) {
        this.userId = userId;
        this.username = username;
        this.connection = connection;
        this.joinTime = LocalDateTime.now();
    }

    public void setConnection(WebSocket connection) {
        this.connection = connection;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public WebSocket getConnection() {
        return connection;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectedUser that = (ConnectedUser) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "ConnectedUser{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", joinTime=" + joinTime +
                '}';
    }
}