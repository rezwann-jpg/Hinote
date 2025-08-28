package com.hinote.client.ui;

import com.hinote.client.models.ChatMessage;
import com.hinote.client.models.DrawingOperation;
import com.hinote.client.models.TextOperation;
import com.hinote.client.network.ConnectionManager;
import com.hinote.client.network.MessageHandler;
import com.hinote.client.ui.components.ChatPanel;
import com.hinote.client.ui.components.DrawingCanvas;
import com.hinote.client.ui.components.ToolsPanel;
import com.hinote.shared.protocol.*;
import com.hinote.shared.utils.IdGenerator;
import com.hinote.shared.utils.JsonUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private VBox drawingCanvas;  // VBox placeholder from FXML
    @FXML private VBox sidePanel;      // Side panel from FXML

    private ConnectionManager connectionManager;
    private String userId;
    private String username;
    private String roomId;

    private ChatPanel chatPanel;
    private DrawingCanvas canvasComponent;
    private ToolsPanel toolsPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeUser();
        initializeComponents();
        initializeNetwork();
    }

    private void initializeUser() {
        userId = IdGenerator.generateUserId();
        String[] adjectives = {"Cool", "Fast", "Smart", "Quick", "Bright", "Swift"};
        String[] nouns = {"Drawer", "Artist", "Writer", "Creator", "Builder", "Maker"};
        Random random = new Random();
        username = adjectives[random.nextInt(adjectives.length)] + nouns[random.nextInt(nouns.length)] + "_" + (100 + random.nextInt(900));
        roomId = "test-room";
    }

    private void initializeComponents() {
        // Initialize chat panel
        chatPanel = new ChatPanel(this);
        sidePanel.getChildren().add(chatPanel.getRoot());

        // Initialize drawing canvas
        canvasComponent = new DrawingCanvas(600, 400);
        canvasComponent.setOnDrawingOperationListener(this::sendDrawingOperation);
        drawingCanvas.getChildren().add(canvasComponent);

        // Initialize tools panel
        toolsPanel = new ToolsPanel();
        // In the tools listener setup:
        toolsPanel.setToolsListener(new ToolsPanel.ToolsListener() {
            @Override
            public void onToolSelected(String tool) {
                canvasComponent.setCurrentTool(tool);
            }
        
            @Override
            public void onColorSelected(String color) {
                canvasComponent.setCurrentColor(color);
            }
        
            @Override
            public void onStrokeWidthChanged(double width) {
                canvasComponent.setStrokeWidth(width);
            }
        
            @Override
            public void onClearCanvas() {
                canvasComponent.clear();
            }
        });
        
        // Add tools panel above the canvas
        drawingCanvas.getChildren().add(0, toolsPanel); // Add at the beginning
    }

    private void initializeNetwork() {
        String serverUrl = "ws://localhost:8080";
        MessageHandler messageHandler = new MessageHandler(this);
        connectionManager = new ConnectionManager(serverUrl, messageHandler.handleMessage());
        sendJoinRoomMessage();
    }

    public void addChatMessage(ChatMessage message) {
        System.out.println("💬 addChatMessage called on thread: " + Thread.currentThread().getName());
        Platform.runLater(() -> {
            String display = String.format("[%s] %s: %s\n",
            message.getTimestamp() != null ? 
                message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            message.getUsername() != null ? message.getUsername() : "Unknown",
            message.getContent() != null ? message.getContent() : "");
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
        Platform.runLater(() -> {
            if (canvasComponent != null) {
                canvasComponent.applyDrawingOperation(operation);
            }
        });
    }

    public void applyTextOperation(TextOperation operation) {
        // To be implemented
        System.out.println("Text operation received: " + operation);
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

    private void sendDrawingOperation(DrawingOperation drawingOp) {
        DrawingOperationProtocol protocol = new DrawingOperationProtocol();
        protocol.setOperationType(drawingOp.getOperationType());
        protocol.setStartX(drawingOp.getStartX());
        protocol.setStartY(drawingOp.getStartY());
        protocol.setEndX(drawingOp.getEndX());
        protocol.setEndY(drawingOp.getEndY());
        protocol.setColor(drawingOp.getColor());
        protocol.setStrokeWidth(drawingOp.getStrokeWidth());
        
        Message message = new Message(
            MessageType.DRAW_OPERATION,
            IdGenerator.generateUniqueId(),
            roomId,
            userId,
            username,
            JsonUtil.toJsonNode(protocol)
        );
        
        if (connectionManager != null && connectionManager.isConnected()) {
            connectionManager.sendMessage(message);
        }
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