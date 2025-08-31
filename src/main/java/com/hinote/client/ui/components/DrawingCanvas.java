package com.hinote.client.ui.components;

import com.hinote.client.models.DrawingOperation;
import com.hinote.client.models.TextOperation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.*;

public class DrawingCanvas extends Canvas {
    private GraphicsContext gc;
    private String currentTool = "PEN";
    private String currentColor = "#000000";
    private double strokeWidth = 2.0;
    private double startX, startY;
    private boolean isDrawing = false;
    private DrawingOperationListener listener;
    
    // Text operations and management
    private Map<String, TextOperation> textOperations = new HashMap<>();
    private String selectedTextId = null;
    
    // Batch operations (for network efficiency)
    private List<DrawingOperation> currentBatch = new ArrayList<>();
    private Timer batchTimer;
    private static final int BATCH_TIME_MS = 100;
    
    // Operation history with stroke grouping
    private List<DrawingOperation> operationHistory = new ArrayList<>();
    private Stack<List<DrawingOperation>> undoStack = new Stack<>();
    private Stack<List<DrawingOperation>> redoStack = new Stack<>();
    
    // For stroke grouping (freehand drawing)
    private List<DrawingOperation> currentStroke = new ArrayList<>();
    private boolean isStrokeActive = false;

    // Preview functionality
    private boolean hasPreview = false;

    public DrawingCanvas(double width, double height) {
        super(width, height);
        this.gc = getGraphicsContext2D();
        setupGraphicsContext();
        setupEventHandlers();
        
        // Add border to make canvas boundaries visible
        setStyle("-fx-border-color: #ccc; -fx-border-width: 2px; -fx-border-style: solid;");
    }

    private void setupGraphicsContext() {
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        gc.setFill(Color.BLACK);
    }

    private void setupEventHandlers() {
        setOnMousePressed(e -> {
            isDrawing = true;
            hasPreview = false;
            startX = e.getX();
            startY = e.getY();
            
            // Handle text tool clicks
            if ("TEXT".equals(currentTool)) {
                handleTextToolClick(startX, startY);
                return; // Don't process as drawing operation
            }
            
            // Start new stroke for pen tool
            if ("PEN".equals(currentTool)) {
                isStrokeActive = true;
                currentStroke.clear();
                drawPoint(startX, startY);
                DrawingOperation firstPoint = new DrawingOperation(
                    "DRAW_LINE", startX, startY, startX, startY, 
                    currentColor, strokeWidth, "current-user"
                );
                currentStroke.add(firstPoint);
            }
        });

        setOnMouseDragged(e -> {
            if (isDrawing && !"TEXT".equals(currentTool)) {
                double currentX = e.getX();
                double currentY = e.getY();
                
                switch (currentTool) {
                    case "PEN":
                        drawLine(startX, startY, currentX, currentY);
                        DrawingOperation strokeOp = new DrawingOperation(
                            "DRAW_LINE", startX, startY, currentX, currentY, 
                            currentColor, strokeWidth, "current-user"
                        );
                        currentStroke.add(strokeOp);
                        startX = currentX;
                        startY = currentY;
                        break;
                    case "ERASER":
                        eraseLine(startX, startY, currentX, currentY);
                        DrawingOperation eraseOp = new DrawingOperation(
                            "ERASE_LINE", startX, startY, currentX, currentY, 
                            "#FFFFFF", strokeWidth * 3, "current-user"
                        );
                        currentStroke.add(eraseOp);
                        startX = currentX;
                        startY = currentY;
                        break;
                    case "LINE":
                    case "RECTANGLE":
                    case "CIRCLE":
                        redrawAllOperations();
                        drawPreview(startX, startY, currentX, currentY);
                        hasPreview = true;
                        break;
                }
            }
        });

        setOnMouseReleased(e -> {
            if (isDrawing) {
                double endX = e.getX();
                double endY = e.getY();
                
                // Handle stroke completion for pen and eraser tools
                if (("PEN".equals(currentTool) || "ERASER".equals(currentTool)) && isStrokeActive) {
                    if (!currentStroke.isEmpty()) {
                        List<DrawingOperation> strokeCopy = new ArrayList<>(currentStroke);
                        undoStack.push(strokeCopy);
                        operationHistory.addAll(currentStroke);
                        addToBatch(currentStroke);
                        redoStack.clear();
                        updateButtonStates();
                    }
                    currentStroke.clear();
                    isStrokeActive = false;
                } else if (!"TEXT".equals(currentTool)) {
                    // Handle other tools (single operation)
                    switch (currentTool) {
                        case "LINE":
                            drawAndSendLine(startX, startY, endX, endY);
                            break;
                        case "RECTANGLE":
                            drawAndSendRectangle(startX, startY, endX, endY);
                            break;
                        case "CIRCLE":
                            drawAndSendCircle(startX, startY, endX, endY);
                            break;
                    }
                }
            }
            isDrawing = false;
            hasPreview = false;
        });
    }

    private void handleTextToolClick(double x, double y) {
        // Check if clicking on existing text
        String clickedTextId = null;
        for (Map.Entry<String, TextOperation> entry : textOperations.entrySet()) {
            TextOperation textOp = entry.getValue();
            if (isPointInTextBounds(x, y, textOp)) {
                clickedTextId = entry.getKey();
                break;
            }
        }
        
        if (clickedTextId != null) {
            // Select existing text
            selectText(clickedTextId);
        } else {
            // Create new text
            createNewText(x, y);
        }
    }

    private boolean isPointInTextBounds(double x, double y, TextOperation textOp) {
        // Simple bounds checking - in a real implementation, you'd calculate actual text bounds
        double textWidth = Math.max(50, textOp.getContent().length() * 8); // Approximate width
        double textHeight = textOp.getFontSize() + 5; // Approximate height
        
        return x >= textOp.getX() && x <= textOp.getX() + textWidth &&
               y >= textOp.getY() - textHeight && y <= textOp.getY() + 5;
    }

    private void selectText(String textId) {
        // Deselect current selection
        selectedTextId = textId;
        
        // Notify listeners
        if (listener != null) {
            TextOperation selectedText = textOperations.get(textId);
            if (selectedText != null) {
                listener.onTextSelected(new TextOperation(selectedText));
            }
        }
        
        redrawAllOperations();
    }

    private void createNewText(double x, double y) {
        // Deselect current selection
        selectedTextId = null;
        
        // Create new text operation
        String textId = "text-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        TextOperation textOp = new TextOperation(textId, x, y, "Double-click to edit");
        textOp.setColor(currentColor);
        textOp.setFontSize(16.0);
        
        // Store text operation
        textOperations.put(textId, textOp);
        
        // Create drawing operation for synchronization
        DrawingOperation drawOp = createDrawingOperationFromText(textOp);
        operationHistory.add(drawOp);
        addToBatch(drawOp);
        
        // Create undo group
        List<DrawingOperation> textOps = new ArrayList<>();
        textOps.add(drawOp);
        undoStack.push(textOps);
        redoStack.clear();
        
        updateButtonStates();
        
        // Select the new text
        selectText(textId);
        
        // Notify listeners
        if (listener != null) {
            listener.onTextOperation(new TextOperation(textOp));
        }
    }

    private DrawingOperation createDrawingOperationFromText(TextOperation textOp) {
        DrawingOperation drawOp = new DrawingOperation();
        drawOp.setOperationType("TEXT_OPERATION");
        drawOp.setStartX(textOp.getX());
        drawOp.setStartY(textOp.getY());
        drawOp.setEndX(textOp.getFontSize());
        drawOp.setEndY(0);
        drawOp.setColor(textOp.getColor());
        drawOp.setStrokeWidth(0);
        drawOp.setUserId(textOp.getUserId());
        drawOp.setOperationId("draw-" + textOp.getOperationId());
        return drawOp;
    }

    // Public methods for text operations
    public void editText(String textId, String newContent) {
        TextOperation textOp = textOperations.get(textId);
        if (textOp != null) {
            TextOperation oldOp = new TextOperation(textOp);
            textOp.setOperationType(TextOperation.TextOperationType.EDIT_TEXT);
            textOp.setContent(newContent);
            
            // Create drawing operation for synchronization
            DrawingOperation drawOp = createDrawingOperationFromText(textOp);
            operationHistory.add(drawOp);
            addToBatch(drawOp);
            
            // Create undo group
            List<DrawingOperation> textOps = new ArrayList<>();
            textOps.add(drawOp);
            undoStack.push(textOps);
            redoStack.clear();
            
            updateButtonStates();
            redrawAllOperations();
            
            if (listener != null) {
                listener.onTextOperation(new TextOperation(textOp));
            }
        }
    }

    public void deleteText(String textId) {
        TextOperation textOp = textOperations.remove(textId);
        if (textOp != null) {
            // Clear selection if deleting selected text
            if (textId.equals(selectedTextId)) {
                selectedTextId = null;
            }
            
            // Create delete operation for synchronization
            TextOperation deleteOp = new TextOperation(textOp);
            deleteOp.setOperationType(TextOperation.TextOperationType.DELETE_TEXT);
            
            DrawingOperation drawOp = createDrawingOperationFromText(deleteOp);
            operationHistory.add(drawOp);
            addToBatch(drawOp);
            
            // Create undo group
            List<DrawingOperation> textOps = new ArrayList<>();
            textOps.add(drawOp);
            undoStack.push(textOps);
            redoStack.clear();
            
            updateButtonStates();
            redrawAllOperations();
            
            if (listener != null) {
                listener.onTextOperation(deleteOp);
            }
        }
    }

    public void updateTextStyle(String textId, String fontFamily, Double fontSize, String fontWeight, String fontStyle, String color) {
        TextOperation textOp = textOperations.get(textId);
        if (textOp != null) {
            TextOperation oldOp = new TextOperation(textOp);
            textOp.setOperationType(TextOperation.TextOperationType.STYLE_TEXT);
            
            if (fontFamily != null) textOp.setFontFamily(fontFamily);
            if (fontSize != null) textOp.setFontSize(fontSize);
            if (fontWeight != null) textOp.setFontWeight(fontWeight);
            if (fontStyle != null) textOp.setFontStyle(fontStyle);
            if (color != null) textOp.setColor(color);
            
            // Create drawing operation for synchronization
            DrawingOperation drawOp = createDrawingOperationFromText(textOp);
            operationHistory.add(drawOp);
            addToBatch(drawOp);
            
            // Create undo group
            List<DrawingOperation> textOps = new ArrayList<>();
            textOps.add(drawOp);
            undoStack.push(textOps);
            redoStack.clear();
            
            updateButtonStates();
            redrawAllOperations();
            
            if (listener != null) {
                listener.onTextOperation(new TextOperation(textOp));
            }
        }
    }

    public TextOperation getSelectedText() {
        if (selectedTextId != null) {
            TextOperation textOp = textOperations.get(selectedTextId);
            return textOp != null ? new TextOperation(textOp) : null;
        }
        return null;
    }

    private void drawPreview(double startX, double startY, double endX, double endY) {
        gc.save();
        gc.setGlobalAlpha(0.7);
        gc.setStroke(Color.web(currentColor));
        gc.setLineWidth(strokeWidth);
        
        switch (currentTool) {
            case "LINE":
                gc.strokeLine(startX, startY, endX, endY);
                break;
            case "RECTANGLE":
                double x = Math.min(startX, endX);
                double y = Math.min(startY, endY);
                double width = Math.abs(endX - startX);
                double height = Math.abs(endY - startY);
                gc.strokeRect(x, y, width, height);
                break;
            case "CIRCLE":
                double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
                break;
        }
        gc.restore();
    }

    private void redrawAllOperations() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        
        // Draw all drawing operations
        for (DrawingOperation op : operationHistory) {
            if (!"TEXT_OPERATION".equals(op.getOperationType())) {
                applyOperationWithoutHistory(op);
            }
        }
        
        // Draw all text operations
        for (TextOperation textOp : textOperations.values()) {
            renderText(textOp);
        }
    }

    private void renderText(TextOperation textOp) {
        gc.save();
        
        // Set font
        Font font = createFont(textOp);
        gc.setFont(font);
        
        // Set color
        gc.setFill(Color.web(textOp.getColor()));
        
        // Draw text
        gc.fillText(textOp.getContent(), textOp.getX(), textOp.getY());
        
        // Draw selection border if selected
        if (textOp.getTextId().equals(selectedTextId)) {
            double textWidth = Math.max(50, textOp.getContent().length() * 8);
            double textHeight = textOp.getFontSize() + 5;
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1);
            gc.strokeRect(textOp.getX(), textOp.getY() - textHeight, textWidth, textHeight + 5);
        }
        
        gc.restore();
    }

    private Font createFont(TextOperation textOp) {
        // Convert font weight
        FontWeight fontWeight = "bold".equals(textOp.getFontWeight()) ? 
            FontWeight.BOLD : FontWeight.NORMAL;
        
        // Convert font style
        FontPosture fontPosture = "italic".equals(textOp.getFontStyle()) ? 
            FontPosture.ITALIC : FontPosture.REGULAR;
        
        return Font.font(
            textOp.getFontFamily(),
            fontWeight,
            fontPosture,
            textOp.getFontSize()
        );
    }

    private void applyOperationWithoutHistory(DrawingOperation operation) {
        if (operation != null) {
            gc.save();
            
            switch (operation.getOperationType()) {
                case "DRAW_LINE":
                    gc.setStroke(Color.web(operation.getColor()));
                    gc.setLineWidth(operation.getStrokeWidth());
                    gc.strokeLine(
                        operation.getStartX(), operation.getStartY(),
                        operation.getEndX(), operation.getEndY()
                    );
                    break;
                    
                case "ERASE_LINE":
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(operation.getStrokeWidth());
                    gc.strokeLine(
                        operation.getStartX(), operation.getStartY(),
                        operation.getEndX(), operation.getEndY()
                    );
                    break;
                    
                case "DRAW_RECTANGLE":
                    gc.setStroke(Color.web(operation.getColor()));
                    gc.setLineWidth(operation.getStrokeWidth());
                    gc.strokeRect(
                        operation.getStartX(), operation.getStartY(),
                        operation.getEndX(), operation.getEndY()
                    );
                    break;
                    
                case "DRAW_CIRCLE":
                    gc.setStroke(Color.web(operation.getColor()));
                    gc.setLineWidth(operation.getStrokeWidth());
                    double centerX = operation.getStartX();
                    double centerY = operation.getStartY();
                    double radius = operation.getEndX();
                    gc.strokeOval(
                        centerX - radius, centerY - radius,
                        radius * 2, radius * 2
                    );
                    break;
                    
                case "CLEAR_CANVAS":
                    gc.clearRect(0, 0, getWidth(), getHeight());
                    break;
            }
            
            gc.restore();
        }
    }

    // Batch operation management
    private void addToBatch(DrawingOperation operation) {
        currentBatch.add(new DrawingOperation(operation));
        startBatchTimer();
    }

    private void addToBatch(List<DrawingOperation> operations) {
        for (DrawingOperation op : operations) {
            currentBatch.add(new DrawingOperation(op));
        }
        startBatchTimer();
    }

    private void startBatchTimer() {
        if (batchTimer == null) {
            batchTimer = new Timer();
            batchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendBatch();
                }
            }, BATCH_TIME_MS);
        }
    }

    private void sendBatch() {
        if (!currentBatch.isEmpty() && listener != null) {
            listener.onBatchOperations(new ArrayList<>(currentBatch));
            currentBatch.clear();
        }
        if (batchTimer != null) {
            batchTimer.cancel();
            batchTimer = null;
        }
    }

    private void updateButtonStates() {
        if (listener != null) {
            javafx.application.Platform.runLater(() -> {
                listener.onOperationHistoryChanged(canUndo(), canRedo());
            });
        }
    }

    private void drawLine(double startX, double startY, double endX, double endY) {
        gc.save();
        gc.setStroke(Color.web(currentColor));
        gc.setLineWidth(strokeWidth);
        gc.strokeLine(startX, startY, endX, endY);
        gc.restore();
        
        if (!"PEN".equals(currentTool) && listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "DRAW_LINE", startX, startY, endX, endY, 
                currentColor, strokeWidth, "current-user"
            );
            addSingleOperation(operation);
        }
    }

    private void drawPoint(double x, double y) {
        gc.save();
        gc.setStroke(Color.web(currentColor));
        gc.setLineWidth(strokeWidth);
        gc.strokeLine(x, y, x, y);
        gc.restore();
        
        if (!"PEN".equals(currentTool) && listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "DRAW_LINE", x, y, x, y, 
                currentColor, strokeWidth, "current-user"
            );
            addSingleOperation(operation);
        }
    }

    private void eraseLine(double startX, double startY, double endX, double endY) {
        gc.save();
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(strokeWidth * 3);
        gc.strokeLine(startX, startY, endX, endY);
        gc.restore();
        
        if (listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "ERASE_LINE", startX, startY, endX, endY, 
                "#FFFFFF", strokeWidth * 3, "current-user"
            );
            addSingleOperation(operation);
        }
    }

    private void addSingleOperation(DrawingOperation operation) {
        operationHistory.add(new DrawingOperation(operation));
        addToBatch(operation);
        
        List<DrawingOperation> singleOpGroup = Collections.singletonList(operation);
        undoStack.push(singleOpGroup);
        redoStack.clear();
        
        updateButtonStates();
        
        if (listener != null) {
            listener.onDrawingOperation(operation);
        }
    }

    private void drawAndSendLine(double startX, double startY, double endX, double endY) {
        gc.save();
        gc.setStroke(Color.web(currentColor));
        gc.setLineWidth(strokeWidth);
        gc.strokeLine(startX, startY, endX, endY);
        gc.restore();
        
        if (listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "DRAW_LINE", startX, startY, endX, endY, 
                currentColor, strokeWidth, "current-user"
            );
            addSingleOperation(operation);
        }
    }

    private void drawAndSendRectangle(double startX, double startY, double endX, double endY) {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        
        gc.save();
        gc.setStroke(Color.web(currentColor));
        gc.setLineWidth(strokeWidth);
        gc.strokeRect(x, y, width, height);
        gc.restore();
        
        if (listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "DRAW_RECTANGLE", x, y, width, height, 
                currentColor, strokeWidth, "current-user"
            );
            addSingleOperation(operation);
        }
    }

    private void drawAndSendCircle(double startX, double startY, double endX, double endY) {
        double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
        
        gc.save();
        gc.setStroke(Color.web(currentColor));
        gc.setLineWidth(strokeWidth);
        gc.strokeOval(startX - radius, startY - radius, radius * 2, radius * 2);
        gc.restore();
        
        if (listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "DRAW_CIRCLE", startX, startY, radius, 0, 
                currentColor, strokeWidth, "current-user"
            );
            operation.setEndX(radius);
            operation.setEndY(0);
            addSingleOperation(operation);
        }
    }

    public List<DrawingOperation> undo() {
        if (!undoStack.isEmpty()) {
            List<DrawingOperation> operationsToUndo = undoStack.pop();
            
            redoStack.push(new ArrayList<>(operationsToUndo));
            
            for (DrawingOperation op : operationsToUndo) {
                operationHistory.removeIf(historyOp -> 
                    historyOp.getOperationId().equals(op.getOperationId()));
            }
            
            redrawAllOperations();
            updateButtonStates();
            
            if (listener != null) {
                listener.onUndo();
            }
            
            return operationsToUndo;
        }
        return Collections.emptyList();
    }
    
    public List<DrawingOperation> redo() {
        if (!redoStack.isEmpty()) {
            List<DrawingOperation> operationsToRedo = redoStack.pop();
            
            undoStack.push(new ArrayList<>(operationsToRedo));
            
            for (DrawingOperation op : operationsToRedo) {
                operationHistory.add(new DrawingOperation(op));
            }
            
            redrawAllOperations();
            updateButtonStates();
            
            if (listener != null) {
                listener.onRedo();
            }
            
            return operationsToRedo;
        }
        return Collections.emptyList();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public DrawingOperation clear() {
        if (!operationHistory.isEmpty()) {
            List<DrawingOperation> allOperations = new ArrayList<>(operationHistory);
            undoStack.push(allOperations);
        }
        
        // Clear text operations
        textOperations.clear();
        selectedTextId = null;
        
        DrawingOperation clearOperation = new DrawingOperation(
            "CLEAR_CANVAS", 0, 0, 0, 0, 
            "#FFFFFF", 0, "current-user"
        );
        
        operationHistory.clear();
        currentStroke.clear();
        currentBatch.clear();
        redoStack.clear();
        gc.clearRect(0, 0, getWidth(), getHeight());

        updateButtonStates();
        
        if (listener != null) {
            listener.onDrawingOperation(clearOperation);
        }
        
        return clearOperation;
    }

    // Method to apply text operations from other clients
    public void applyTextOperation(TextOperation textOp) {
        if (textOp != null) {
            switch (textOp.getOperationType()) {
                case CREATE_TEXT:
                    textOperations.put(textOp.getTextId(), new TextOperation(textOp));
                    break;
                case EDIT_TEXT:
                    TextOperation existing = textOperations.get(textOp.getTextId());
                    if (existing != null) {
                        existing.setContent(textOp.getContent());
                    } else {
                        textOperations.put(textOp.getTextId(), new TextOperation(textOp));
                    }
                    break;
                case DELETE_TEXT:
                    textOperations.remove(textOp.getTextId());
                    if (textOp.getTextId().equals(selectedTextId)) {
                        selectedTextId = null;
                    }
                    break;
                case STYLE_TEXT:
                    TextOperation styleExisting = textOperations.get(textOp.getTextId());
                    if (styleExisting != null) {
                        styleExisting.setFontFamily(textOp.getFontFamily());
                        styleExisting.setFontSize(textOp.getFontSize());
                        styleExisting.setFontWeight(textOp.getFontWeight());
                        styleExisting.setFontStyle(textOp.getFontStyle());
                        styleExisting.setColor(textOp.getColor());
                    }
                    break;
            }
            redrawAllOperations();
        }
    }

    // Public methods for tool selection
    public void setCurrentTool(String tool) {
        this.currentTool = tool;
    }

    public void setCurrentColor(String color) {
        this.currentColor = color;
    }

    public void setStrokeWidth(double width) {
        this.strokeWidth = width;
    }

    public void applyDrawingOperation(DrawingOperation operation) {
        if (operation != null) {
            operationHistory.add(new DrawingOperation(operation));
            applyOperationWithoutHistory(operation);
        }
    }

    public void applyBatchOperations(List<DrawingOperation> operations) {
        if (operations != null) {
            for (DrawingOperation operation : operations) {
                operationHistory.add(new DrawingOperation(operation));
            }
            redrawAllOperations();
        }
    }

    public void removeOperations(List<DrawingOperation> operationsToRemove) {
        for (DrawingOperation op : operationsToRemove) {
            operationHistory.removeIf(historyOp -> 
                historyOp.getOperationId().equals(op.getOperationId()));
        }
        redrawAllOperations();
        updateButtonStates();
    }

    public void addOperations(List<DrawingOperation> operationsToAdd) {
        for (DrawingOperation op : operationsToAdd) {
            operationHistory.add(new DrawingOperation(op));
        }
        redrawAllOperations();
        updateButtonStates();
    }

    public void setOnDrawingOperationListener(DrawingOperationListener listener) {
        this.listener = listener;
    }

    public interface DrawingOperationListener {
        void onDrawingOperation(DrawingOperation operation);
        void onTextOperation(TextOperation operation);
        void onBatchOperations(List<DrawingOperation> operations);
        void onUndo();
        void onRedo();
        void onTextSelected(TextOperation operation);
        default void onOperationHistoryChanged(boolean canUndo, boolean canRedo) {
        }
    }
}