package com.hinote.client.models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomId;
    private String roomName;
    private List<User> participants;
    private List<ChatMessage> chatHistory;
    private List<DrawingOperation> drawingHistory;
    private List<TextOperation> textHistory;

    public Room() {
        this.participants = new ArrayList<>();
        this.chatHistory = new ArrayList<>();
        this.drawingHistory = new ArrayList<>();
        this.textHistory = new ArrayList<>();
    }

    public Room(String roomId, String roomName) {
        this();
        this.roomId = roomId;
        this.roomName = roomName;
    }

    // Getters and setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public List<User> getParticipants() { return participants; }
    public void setParticipants(List<User> participants) { this.participants = participants; }

    public List<ChatMessage> getChatHistory() { return chatHistory; }
    public void setChatHistory(List<ChatMessage> chatHistory) { this.chatHistory = chatHistory; }

    public List<DrawingOperation> getDrawingHistory() { return drawingHistory; }
    public void setDrawingHistory(List<DrawingOperation> drawingHistory) { this.drawingHistory = drawingHistory; }

    public List<TextOperation> getTextHistory() { return textHistory; }
    public void setTextHistory(List<TextOperation> textHistory) { this.textHistory = textHistory; }

    public void addParticipant(User user) {
        if (!participants.contains(user)) {
            participants.add(user);
        }
    }

    public void removeParticipant(User user) {
        participants.remove(user);
    }

    public void addChatMessage(ChatMessage message) {
        chatHistory.add(message);
    }

    public void addDrawingOperation(DrawingOperation operation) {
        drawingHistory.add(operation);
    }

    public void addTextOperation(TextOperation operation) {
        textHistory.add(operation);
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId='" + roomId + '\'' +
                ", roomName='" + roomName + '\'' +
                ", participants=" + participants.size() +
                ", chatHistory=" + chatHistory.size() +
                ", drawingHistory=" + drawingHistory.size() +
                ", textHistory=" + textHistory.size() +
                '}';
    }
}