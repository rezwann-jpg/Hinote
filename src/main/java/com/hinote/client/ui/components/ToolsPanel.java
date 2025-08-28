package com.hinote.client.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ToolsPanel extends HBox {
    private ToggleGroup toolGroup;
    private ToggleButton penButton;
    private ToggleButton eraserButton;
    private ToggleButton rectangleButton;
    private ToggleButton circleButton;
    private ToggleButton lineButton;
    private ColorPicker colorPicker;
    private Slider strokeWidthSlider;
    private Button clearButton;
    private ToolsListener listener;

    public ToolsPanel() {
        setupUI();
        setupEventHandlers();
        setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 1 0 0;");
    }

    private void setupUI() {
        // Header
        Label header = new Label("Drawing Tools");
        header.setFont(Font.font(14));
        header.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        
        // Tool buttons with icons/text
        toolGroup = new ToggleGroup();
        
        penButton = createToolButton("✏️ Pen", "PEN");
        eraserButton = createToolButton("🧽 Eraser", "ERASER");
        rectangleButton = createToolButton("⬜ Rectangle", "RECTANGLE");
        circleButton = createToolButton("⭕ Circle", "CIRCLE");
        lineButton = createToolButton("📏 Line", "LINE");
        
        penButton.setSelected(true);
        
        VBox toolsBox = new VBox(5);
        toolsBox.getChildren().addAll(penButton, eraserButton, lineButton, rectangleButton, circleButton);
        toolsBox.setPadding(new Insets(5));

        // Color picker section
        Label colorLabel = new Label("Color:");
        colorLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setMaxWidth(Double.MAX_VALUE);

        // Stroke width section
        Label widthLabel = new Label("Stroke Width:");
        widthLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 5 0;");
        strokeWidthSlider = new Slider(1, 20, 2);
        strokeWidthSlider.setShowTickMarks(true);
        strokeWidthSlider.setShowTickLabels(true);
        strokeWidthSlider.setMajorTickUnit(5);
        strokeWidthSlider.setBlockIncrement(1);
        
        HBox widthBox = new HBox(10, new Label("1"), strokeWidthSlider, new Label("20"));
        widthBox.setAlignment(Pos.CENTER);

        // Clear button
        clearButton = new Button("🗑️ Clear Canvas");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
        
        // Add all components
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.getChildren().addAll(
            header,
            new Separator(),
            toolsBox,
            new Separator(),
            colorLabel,
            colorPicker,
            new Separator(),
            widthLabel,
            widthBox,
            new Separator(),
            clearButton
        );
    }

    private ToggleButton createToolButton(String text, String toolType) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(toolGroup);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(35);
        button.setStyle("-fx-alignment: CENTER-LEFT;");
        return button;
    }

    private void setupEventHandlers() {
        toolGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && listener != null) {
                ToggleButton selected = (ToggleButton) newValue;
                String tool = "PEN";
                
                if (selected == penButton) tool = "PEN";
                else if (selected == eraserButton) tool = "ERASER";
                else if (selected == rectangleButton) tool = "RECTANGLE";
                else if (selected == circleButton) tool = "CIRCLE";
                else if (selected == lineButton) tool = "LINE";
                
                listener.onToolSelected(tool);
            }
        });

        colorPicker.setOnAction(e -> {
            if (listener != null) {
                listener.onColorSelected("#" + colorPicker.getValue().toString().substring(2, 8));
            }
        });

        strokeWidthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (listener != null) {
                listener.onStrokeWidthChanged(newValue.doubleValue());
            }
        });

        clearButton.setOnAction(e -> {
            if (listener != null) {
                listener.onClearCanvas();
            }
        });
    }

    public void setToolsListener(ToolsListener listener) {
        this.listener = listener;
    }

    // Remove the incorrect method and add proper getters
    public String getCurrentSelectedTool() {
        ToggleButton selected = (ToggleButton) toolGroup.getSelectedToggle();
        if (selected == penButton) return "PEN";
        else if (selected == eraserButton) return "ERASER";
        else if (selected == rectangleButton) return "RECTANGLE";
        else if (selected == circleButton) return "CIRCLE";
        else if (selected == lineButton) return "LINE";
        return "PEN"; // default
    }

    public String getCurrentColor() {
        return "#" + colorPicker.getValue().toString().substring(2, 8);
    }

    public double getCurrentStrokeWidth() {
        return strokeWidthSlider.getValue();
    }

    public interface ToolsListener {
        void onToolSelected(String tool);
        void onColorSelected(String color);
        void onStrokeWidthChanged(double width);
        void onClearCanvas();
    }
}