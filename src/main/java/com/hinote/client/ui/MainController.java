package com.hinote.client.ui;

import com.hinote.client.models.ChatMessage;
import com.hinote.client.models.DrawingOperation;
import com.hinote.client.models.TextOperation;
import com.hinote.client.network.ConnectionManager;
import com.hinote.client.network.MessageHandler;
import com.hinote.shared.protocol.ChatMessageProtocol;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.protocol.MessageType;
import com.hinote.shared.utils.IdGenerator;
import com.hinote.shared.utils.JsonUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    private ConnectionManager connectionManager;
    private String userId;
    private String username;
    private String roomId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userId = IdGenerator.generateUserId();
        username = "User_" + userId.substring(Math.min(5, userId.length()), 
            Math.min(10, userId.length())); // Simple username for demo
        roomId = IdGenerator.generateRoomId();
        String serverUrl = "ws://localhost:8080"; // Should come from ClientConfig
        
        MessageHandler messageHandler = new MessageHandler(this);
        
        connectionManager = new ConnectionManager(serverUrl, messageHandler.handleMessage());
        
        sendJoinRoomMessage();
    }

    public void addChatMessage(ChatMessage message) {
        Platform.runLater(() -> {
            String display = String.format("[%s] %s: %s\n",
                message.getTimestamp() != null ? message.getTimestamp().toString() : LocalDateTime.now(),
                message.getUsername() != null ? message.getUsername() : "Unknown",
                message.getContent() != null ? message.getContent() : "");
            if (chatArea != null) {
                chatArea.appendText(display);
            }
        });
    }

    public void showSystemMessage(String content) {
        Platform.runLater(() -> {
            if (chatArea != null) {
                chatArea.appendText("[System] " + content + "\n");
            }
        });
    }

    public void showErrorMessage(String content) {
        Platform.runLater(() -> {
            if (chatArea != null) {
                chatArea.appendText("[Error] " + content + "\n");
            }
        });
    }

    public void updateParticipants(String roomId) {
        // Placeholder for Update participants panel
        Platform.runLater(() -> showSystemMessage("Participants updated for room: " + roomId));
    }

    public void applyDrawingOperation(DrawingOperation operation) {
        // Placeholder
        System.out.println("Drawing operation received: " + operation);
    }

    public void applyTextOperation(TextOperation operation) {
        // Placeholder
        System.out.println("Text operation received: " + operation);
    }

    @FXML
    public void sendChatMessage() {
        if (chatInput != null) {
            String content = chatInput.getText().trim();
            if (!content.isEmpty() && connectionManager != null) {
                Message message = new Message(
                    MessageType.CHAT_MESSAGE,
                    IdGenerator.generateUniqueId(),
                    roomId,
                    userId,
                    username,
                    JsonUtil.toJsonNode(ChatMessageProtocol.userMessage(content))
                );
                connectionManager.sendMessage(message);
                chatInput.clear();
            }
        }
    }

    private void sendJoinRoomMessage() {
        if (connectionManager != null) {
            Message message = new Message(
                MessageType.JOIN_ROOM,
                IdGenerator.generateUniqueId(),
                roomId,
                userId,
                username,
                null
            );
            connectionManager.sendMessage(message);
        }
    }
    
    // Getter methods
    public String getUsername() {
        return username;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getRoomId() {
        return roomId;
    }
}