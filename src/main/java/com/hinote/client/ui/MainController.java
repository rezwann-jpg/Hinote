package com.hinote.client.ui;

import com.hinote.client.models.ChatMessage;
import com.hinote.client.models.DrawingOperation;
import com.hinote.client.models.TextOperation;
import com.hinote.client.network.ConnectionManager;
import com.hinote.client.network.MessageHandler;
import com.hinote.client.ui.components.ChatPanel;
import com.hinote.client.ui.components.DrawingCanvas;
import com.hinote.client.ui.components.TextToolsPanel;
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
import java.util.ArrayList;
import java.util.List;
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

    private TextToolsPanel textToolsPanel;

    private boolean isProcessingRemoteOperation = false;
    private String selectedTextId = null;

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
        // Update the canvas listener to handle undo/redo events:
        canvasComponent.setOnDrawingOperationListener(new DrawingCanvas.DrawingOperationListener() {
            @Override
            public void onDrawingOperation(DrawingOperation operation) {
                sendDrawingOperation(operation);
            }
        
            @Override
            public void onBatchOperations(List<DrawingOperation> operations) {
                sendBatchOperations(operations);
            }
        
            @Override
            public void onUndo() {
                Platform.runLater(() -> {
                    toolsPanel.updateUndoRedoButtons(
                        canvasComponent.canUndo(), 
                        canvasComponent.canRedo()
                    );
                });
                // Optionally send undo event to other clients
            }
        
            @Override
            public void onRedo() {
                Platform.runLater(() -> {
                    toolsPanel.updateUndoRedoButtons(
                        canvasComponent.canUndo(), 
                        canvasComponent.canRedo()
                    );
                });
                // Optionally send redo event to other clients
            }

            @Override
            public void onOperationHistoryChanged(boolean canUndo, boolean canRedo) {
                Platform.runLater(() -> {
                    toolsPanel.updateUndoRedoButtons(canUndo, canRedo);
                });
            }

            @Override
            public void onTextOperation(TextOperation operation) {
                sendTextOperation(operation);
            }

            @Override
            public void onTextSelected(TextOperation operation) {
                // Show text tools panel when text is selected
                selectedTextId = operation.getTextId();
                if (!drawingCanvas.getChildren().contains(textToolsPanel)) {
                    drawingCanvas.getChildren().add(textToolsPanel);
                }
                textToolsPanel.setTextContent(operation.getContent());
            }
        });
        drawingCanvas.getChildren().add(canvasComponent);

        // Initialize tools panel
        toolsPanel = new ToolsPanel();
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
        
             // Update the tools listener methods:
            @Override
            public void onUndo() {
                if (!isProcessingRemoteOperation) {
                    List<DrawingOperation> undoneOperations = canvasComponent.undo();
                    if (!undoneOperations.isEmpty()) {
                        sendUndoOperations(undoneOperations);
                    }
                }
            }
        
            @Override
            public void onRedo() {
                if (!isProcessingRemoteOperation) {
                    List<DrawingOperation> redoneOperations = canvasComponent.redo();
                    if (!redoneOperations.isEmpty()) {
                        sendRedoOperations(redoneOperations);
                    }
                }
            }
        });

        textToolsPanel = new TextToolsPanel();
        textToolsPanel.setToolsListener(new TextToolsPanel.TextToolsListener() {
            @Override
            public void onTextEditRequested() {
                if (canvasComponent != null && canvasComponent.getSelectedText() != null) {
                    textToolsPanel.setTextContent(canvasComponent.getSelectedText().getContent());
                }
            }
        
            @Override
            public void onTextEdited(String newText) {
                if (selectedTextId != null && canvasComponent != null) {
                    canvasComponent.editText(selectedTextId, newText);
                }
            }
        
            @Override
            public void onTextDeleted() {
                if (selectedTextId != null && canvasComponent != null) {
                    canvasComponent.deleteText(selectedTextId);
                }
            }

            @Override
            public void onTextStyleChanged(String fontFamily, Double fontSize, String fontWeight, String fontStyle, String color) {
                if (selectedTextId != null && canvasComponent != null) {
                    canvasComponent.updateTextStyle(selectedTextId, fontFamily, fontSize, fontWeight, fontStyle, color);
                }
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
        Platform.runLater(() -> {
            if (canvasComponent != null) {
                canvasComponent.applyTextOperation(operation);
            }
        });
    }

    private void sendTextOperation(TextOperation textOp) {
        TextOperationProtocol protocol = new TextOperationProtocol();
        protocol.setTextId(textOp.getTextId());
        protocol.setOperationType(TextOperationProtocol.TextOperationType.valueOf(textOp.getOperationType().name()));
        protocol.setX(textOp.getX());
        protocol.setY(textOp.getY());
        protocol.setContent(textOp.getContent());
        protocol.setFontSize(textOp.getFontSize());
        protocol.setFontFamily(textOp.getFontFamily());
        protocol.setFontWeight(textOp.getFontWeight());
        protocol.setFontStyle(textOp.getFontStyle());
        protocol.setColor(textOp.getColor());
        protocol.setRotation(textOp.getRotation());
        
        Message message = new Message(
            MessageType.TEXT_OPERATION,
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
    // Ensure operation has an ID
    if (drawingOp.getOperationId() == null || drawingOp.getOperationId().isEmpty()) {
        drawingOp.setOperationId("op-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000));
    }
    
    DrawingOperationProtocol protocol = new DrawingOperationProtocol();
        protocol.setOperationType(drawingOp.getOperationType());
        protocol.setStartX(drawingOp.getStartX());
        protocol.setStartY(drawingOp.getStartY());
        protocol.setEndX(drawingOp.getEndX());
        protocol.setEndY(drawingOp.getEndY());
        protocol.setColor(drawingOp.getColor());
        protocol.setStrokeWidth(drawingOp.getStrokeWidth());
        protocol.setOperationId(drawingOp.getOperationId()); // Add this line

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

    private void sendBatchOperations(List<DrawingOperation> operations) {
        // Create a batch protocol
        BatchDrawingOperationProtocol batchProtocol = new BatchDrawingOperationProtocol();
        List<DrawingOperationProtocol> protocols = new ArrayList<>();
        
        for (DrawingOperation operation : operations) {
            // Ensure each operation has an ID
            if (operation.getOperationId() == null || operation.getOperationId().isEmpty()) {
                operation.setOperationId("op-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000));
            }

            DrawingOperationProtocol protocol = new DrawingOperationProtocol();
            protocol.setOperationType(operation.getOperationType());
            protocol.setStartX(operation.getStartX());
            protocol.setStartY(operation.getStartY());
            protocol.setEndX(operation.getEndX());
            protocol.setEndY(operation.getEndY());
            protocol.setColor(operation.getColor());
            protocol.setStrokeWidth(operation.getStrokeWidth());
            protocol.setOperationId(operation.getOperationId());
            protocols.add(protocol);
        }

        batchProtocol.setOperations(protocols);

        Message message = new Message(
            MessageType.BATCH_DRAW_OPERATION,
            IdGenerator.generateUniqueId(),
            roomId,
            userId,
            username,
            JsonUtil.toJsonNode(batchProtocol)
        );

        if (connectionManager != null && connectionManager.isConnected()) {
            connectionManager.sendMessage(message);
        }
    }


    private void sendUndoOperations(List<DrawingOperation> operations) {
        UndoOperationProtocol undoProtocol = new UndoOperationProtocol();
        List<String> operationIds = new ArrayList<>();
        
        for (DrawingOperation op : operations) {
            if (op.getOperationId() != null) {
                operationIds.add(op.getOperationId());
            }
        }

        undoProtocol.setOperationIds(operationIds);

        Message message = new Message(
            MessageType.UNDO_OPERATION,
            IdGenerator.generateUniqueId(),
            roomId,
            userId,
            username,
            JsonUtil.toJsonNode(undoProtocol)
        );

        if (connectionManager != null && connectionManager.isConnected()) {
            connectionManager.sendMessage(message);
        }
    }

    private void sendRedoOperations(List<DrawingOperation> operations) {
    // Send the operations to redo
        sendBatchOperations(operations);
    }

    private void sendClearOperation(DrawingOperation clearOperation) {
        Message message = new Message(
            MessageType.CLEAR_OPERATION,
            IdGenerator.generateUniqueId(),
            roomId,
            userId,
            username,
            JsonUtil.toJsonNode(clearOperation)
        );

        if (connectionManager != null && connectionManager.isConnected()) {
            connectionManager.sendMessage(message);
        }
    }

    public void performRemoteUndo(List<DrawingOperation> operationsToRemove) {
        isProcessingRemoteOperation = true;
        try {
            if (canvasComponent != null) {
                canvasComponent.removeOperations(operationsToRemove);
                Platform.runLater(() -> {
                    if (toolsPanel != null) {
                        toolsPanel.updateUndoRedoButtons(
                            canvasComponent.canUndo(), 
                            canvasComponent.canRedo()
                        );
                    }
                });
            }
        } finally {
            isProcessingRemoteOperation = false;
        }
    }

    public void performRemoteRedo(List<DrawingOperation> operationsToAdd) {
        isProcessingRemoteOperation = true;
        try {
            if (canvasComponent != null) {
                canvasComponent.addOperations(operationsToAdd);
                Platform.runLater(() -> {
                    if (toolsPanel != null) {
                        toolsPanel.updateUndoRedoButtons(
                            canvasComponent.canUndo(), 
                            canvasComponent.canRedo()
                        );
                    }
                });
            }
        } finally {
            isProcessingRemoteOperation = false;
        }
    }

    public void performRemoteClear() {
        isProcessingRemoteOperation = true;
        try {
            if (canvasComponent != null) {
                canvasComponent.clear();
                Platform.runLater(() -> {
                    if (toolsPanel != null) {
                        toolsPanel.updateUndoRedoButtons(
                            canvasComponent.canUndo(), 
                            canvasComponent.canRedo()
                        );
                    }
                });
            }
        } finally {
            isProcessingRemoteOperation = false;
        }
    }

    public void applyBatchOperations(List<DrawingOperation> operations) {
        Platform.runLater(() -> {
            if (canvasComponent != null) {
                canvasComponent.applyBatchOperations(operations);
            }
        });
    }

    // Getters
    public String getUsername() { return username; }
    public String getUserId() { return userId; }
    public String getRoomId() { return roomId; }
}
