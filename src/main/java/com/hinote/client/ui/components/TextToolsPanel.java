package com.hinote.client.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TextToolsPanel extends VBox {
    private TextField fontFamilyField;
    private TextField fontSizeField;
    private ChoiceBox<String> fontWeightChoice;
    private ChoiceBox<String> fontStyleChoice;
    private ColorPicker colorPicker;
    private Button editButton;
    private Button deleteButton;
    private TextArea editTextArea;
    private Button saveEditButton;
    
    private TextToolsListener listener;

    public TextToolsPanel() {
        setupUI();
        setupEventHandlers();
    }

    private void setupUI() {
        // Font family
        Label fontFamilyLabel = new Label("Font Family:");
        fontFamilyField = new TextField("Arial");
        
        // Font size
        Label fontSizeLabel = new Label("Font Size:");
        fontSizeField = new TextField("16");
        fontSizeField.setPrefWidth(60);
        
        // Font weight
        Label fontWeightLabel = new Label("Weight:");
        fontWeightChoice = new ChoiceBox<>();
        fontWeightChoice.getItems().addAll("normal", "bold");
        fontWeightChoice.setValue("normal");
        
        // Font style
        Label fontStyleLabel = new Label("Style:");
        fontStyleChoice = new ChoiceBox<>();
        fontStyleChoice.getItems().addAll("normal", "italic");
        fontStyleChoice.setValue("normal");
        
        // Color
        Label colorLabel = new Label("Color:");
        colorPicker = new ColorPicker(javafx.scene.paint.Color.BLACK);
        
        // Edit controls
        editButton = new Button("Edit Text");
        deleteButton = new Button("Delete Text");
        
        // Text editing area
        editTextArea = new TextArea();
        editTextArea.setPrefRowCount(3);
        editTextArea.setVisible(false);
        saveEditButton = new Button("Save Changes");
        saveEditButton.setVisible(false);
        
        // Layout
        VBox fontFamilyBox = new VBox(5, fontFamilyLabel, fontFamilyField);
        VBox fontSizeBox = new VBox(5, fontSizeLabel, fontSizeField);
        VBox fontWeightBox = new VBox(5, fontWeightLabel, fontWeightChoice);
        VBox fontStyleBox = new VBox(5, fontStyleLabel, fontStyleChoice);
        VBox colorBox = new VBox(5, colorLabel, colorPicker);
        
        HBox fontControls = new HBox(10, fontFamilyBox, fontSizeBox, fontWeightBox, fontStyleBox, colorBox);
        HBox actionButtons = new HBox(10, editButton, deleteButton);
        
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.getChildren().addAll(
            new Label("Text Properties:"),
            fontControls,
            actionButtons,
            editTextArea,
            saveEditButton
        );
    }

    private void setupEventHandlers() {
        editButton.setOnAction(e -> {
            if (listener != null) {
                editTextArea.setVisible(true);
                saveEditButton.setVisible(true);
                listener.onTextEditRequested();
            }
        });
        
        saveEditButton.setOnAction(e -> {
            if (listener != null) {
                listener.onTextEdited(editTextArea.getText());
                editTextArea.setVisible(false);
                saveEditButton.setVisible(false);
                editTextArea.clear();
            }
        });
        
        deleteButton.setOnAction(e -> {
            if (listener != null) {
                listener.onTextDeleted();
            }
        });
        
        // Apply style changes
        fontFamilyField.setOnAction(e -> applyStyleChanges());
        fontSizeField.setOnAction(e -> applyStyleChanges());
        fontWeightChoice.setOnAction(e -> applyStyleChanges());
        fontStyleChoice.setOnAction(e -> applyStyleChanges());
        colorPicker.setOnAction(e -> applyStyleChanges());
    }

    private void applyStyleChanges() {
        if (listener != null) {
            try {
                Double fontSize = Double.parseDouble(fontSizeField.getText());
                listener.onTextStyleChanged(
                    fontFamilyField.getText(),
                    fontSize,
                    fontWeightChoice.getValue(),
                    fontStyleChoice.getValue(),
                    "#" + colorPicker.getValue().toString().substring(2, 8)
                );
            } catch (NumberFormatException ex) {
                // Handle invalid font size
            }
        }
    }

    public void setTextContent(String content) {
        editTextArea.setText(content);
    }

    public void setToolsListener(TextToolsListener listener) {
        this.listener = listener;
    }

    public interface TextToolsListener {
        void onTextEditRequested();
        void onTextEdited(String newText);
        void onTextDeleted();
        void onTextStyleChanged(String fontFamily, Double fontSize, String fontWeight, String fontStyle, String color);
    }
}