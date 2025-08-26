// com.hinote.client.ui.MainController
package com.hinote.client.ui;

import com.hinote.client.models.ChatMessage;
import com.hinote.client.models.DrawingOperation;
import com.hinote.client.models.TextOperation;
import com.hinote.client.network.ConnectionManager;
import com.hinote.client.network.MessageHandler;
import com.hinote.client.ui.components.ChatPanel;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.protocol.MessageType;
import com.hinote.shared.utils.IdGenerator;
import com.hinote.shared.utils.JsonUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private VBox drawingCanvas;  // Will be replaced with actual canvas later
    @FXML private VBox sidePanel;      // ← Must have fx:id="sidePanel" in main.fxml

    private ConnectionManager connectionManager;
    private String userId;
    private String username;
    private String roomId;

    private ChatPanel chatPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userId = IdGenerator.generateUserId();
        username = "User_" + userId.substring(5, Math.min(10, userId.length()));
        roomId = "test-room";

        // Initialize chat panel
        chatPanel = new ChatPanel(this);
        sidePanel.getChildren().add(chatPanel.getRoot());

        // Connect to server
        String serverUrl = "ws://localhost:8080";
        MessageHandler messageHandler = new MessageHandler(this);
        connectionManager = new ConnectionManager(serverUrl, messageHandler.handleMessage());

        sendJoinRoomMessage();
    }

    public void addChatMessage(ChatMessage message) {
        Platform.runLater(() -> {
            String time = message.getTimestamp() != null ? message.getTimestamp().toString() : LocalDateTime.now().toString();
            String display = String.format("[%s] %s: %s", time, message.getUsername(), message.getContent());
            chatPanel.appendMessage(display);
        });
    }

    public void showSystemMessage(String content) {
        Platform.runLater(() -> chatPanel.appendMessage("[System] " + content));
    }

    public void showErrorMessage(String content) {
        Platform.runLater(() -> chatPanel.appendMessage("[Error] " + content));
    }

    public void updateParticipants(String roomId) {
        Platform.runLater(() -> showSystemMessage("Participants updated"));
    }

    public void applyDrawingOperation(DrawingOperation operation) {
        // To be implemented
    }

    public void applyTextOperation(TextOperation operation) {
        // To be implemented
    }

    @FXML
    public void sendChatMessage() {
        String content = chatPanel.getInputText();
        if (content.isEmpty() || !connectionManager.isConnected()) return;

        Message message = new Message(
            MessageType.CHAT_MESSAGE,
            IdGenerator.generateUniqueId(),
            roomId,
            userId,
            username,
            JsonUtil.toJsonNode(new com.hinote.shared.protocol.ChatMessageProtocol(content, false))
        );

        connectionManager.sendMessage(message);
        chatPanel.clearInput();
    }

    private void sendJoinRoomMessage() {
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

    // Getters
    public String getUsername() { return username; }
    public String getUserId() { return userId; }
    public String getRoomId() { return roomId; }
}