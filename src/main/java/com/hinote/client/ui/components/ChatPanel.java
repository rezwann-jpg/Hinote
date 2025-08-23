package com.hinote.client.ui.components;

import com.hinote.client.ui.MainController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatPanel implements Initializable {
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    private MainController mainController;

    public ChatPanel(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind chatArea and chatInput to MainController's methods
        chatInput.setOnAction(event -> mainController.sendChatMessage());
    }

    public void appendMessage(String message) {
        chatArea.appendText(message);
    }

    public void clearInput() {
        chatInput.clear();
    }
}