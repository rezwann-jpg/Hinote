package com.hinote.client.ui.components;

import com.hinote.client.ui.MainController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ChatPanel {
    @FXML private VBox root;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;

    private final MainController mainController;

    public ChatPanel(MainController mainController) {
        this.mainController = mainController;
        
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/hinote/fxml/chat-panel.fxml"));
            fxmlLoader.setController(this);
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load chat panel", e);
        }
    }

    public VBox getRoot() {
        return root;
    }

    public void appendMessage(String message) {
        chatArea.appendText(message + "\n");
    }

    public void clearInput() {
        chatInput.clear();
    }

    public String getInputText() {
        return chatInput.getText().trim();
    }

    @FXML
    private void sendChatMessage() {
        mainController.sendChatMessage();
    }
}