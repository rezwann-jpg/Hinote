package com.hinote.client.ui.components;

import com.hinote.client.models.DrawingOperation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;
import java.util.List;

public class DrawingCanvas extends Canvas {
    private GraphicsContext gc;
    private String currentTool = "PEN";
    private String currentColor = "#000000";
    private double strokeWidth = 2.0;
    private double startX, startY;
    private boolean isDrawing = false;
    private DrawingOperationListener listener;
    
    // Operation history for proper redrawing
    private List<DrawingOperation> operationHistory = new ArrayList<>();
    
    // For preview functionality
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
        gc.setFill(Color.WHITE);
    }

    private void setupEventHandlers() {
        setOnMousePressed(e -> {
            isDrawing = true;
            hasPreview = false;
            startX = e.getX();
            startY = e.getY();
            
            // For point-based tools
            if ("PEN".equals(currentTool)) {
                drawPoint(startX, startY);
            }
        });

        setOnMouseDragged(e -> {
            if (isDrawing) {
                double currentX = e.getX();
                double currentY = e.getY();
                
                switch (currentTool) {
                    case "PEN":
                        drawLine(startX, startY, currentX, currentY);
                        startX = currentX;
                        startY = currentY;
                        break;
                    case "ERASER":
                        eraseLine(startX, startY, currentX, currentY);
                        startX = currentX;
                        startY = currentY;
                        break;
                    case "LINE":
                    case "RECTANGLE":
                    case "CIRCLE":
                        // Clear canvas and redraw all operations, then draw preview
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
                
                switch (currentTool) {
                    case "PEN":
                        // Already handled in drag
                        break;
                    case "ERASER":
                        // Already handled in drag
                        break;
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
            isDrawing = false;
            hasPreview = false;
        });
    }

    private void drawPreview(double startX, double startY, double endX, double endY) {
        gc.save();
        gc.setGlobalAlpha(0.7); // Semi-transparent preview
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
        // Clear canvas
        gc.clearRect(0, 0, getWidth(), getHeight());
        
        // Redraw all operations from history
        for (DrawingOperation op : operationHistory) {
            applyOperationWithoutHistory(op);
        }
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
                    // For rectangle: startX, startY = top-left corner, endX, endY = width, height
                    gc.strokeRect(
                        operation.getStartX(), operation.getStartY(),
                        operation.getEndX(), operation.getEndY()
                    );
                    break;
                    
                case "DRAW_CIRCLE":
                    gc.setStroke(Color.web(operation.getColor()));
                    gc.setLineWidth(operation.getStrokeWidth());
                    // For circle: startX, startY = center, endX = radius
                    double centerX = operation.getStartX();
                    double centerY = operation.getStartY();
                    double radius = operation.getEndX(); // radius stored in endX
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

    private void drawLine(double startX, double startY, double endX, double endY) {
        gc.save();
        gc.setStroke(Color.web(currentColor));
        gc.setLineWidth(strokeWidth);
        gc.strokeLine(startX, startY, endX, endY);
        gc.restore();
        
        // Notify listener
        if (listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "DRAW_LINE", startX, startY, endX, endY, 
                currentColor, strokeWidth, "current-user"
            );
            operationHistory.add(operation);
            listener.onDrawingOperation(operation);
        }
    }

    private void drawPoint(double x, double y) {
        gc.save();
        gc.setStroke(Color.web(currentColor));
        gc.setLineWidth(strokeWidth);
        gc.strokeLine(x, y, x, y);
        gc.restore();
        
        if (listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "DRAW_LINE", x, y, x, y, 
                currentColor, strokeWidth, "current-user"
            );
            operationHistory.add(operation);
            listener.onDrawingOperation(operation);
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
            operationHistory.add(operation);
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
            operationHistory.add(operation);
            listener.onDrawingOperation(operation);
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
            operationHistory.add(operation);
            listener.onDrawingOperation(operation);
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
            // Store the radius in endX for consistency
            operation.setEndX(radius);
            operation.setEndY(0); // Not used for circles
            operationHistory.add(operation);
            listener.onDrawingOperation(operation);
        }
    }

    public void setCurrentTool(String tool) {
        this.currentTool = tool;
    }

    public void setCurrentColor(String color) {
        this.currentColor = color;
    }

    public void setStrokeWidth(double width) {
        this.strokeWidth = width;
    }

    public void clear() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        operationHistory.clear();
        if (listener != null) {
            DrawingOperation operation = new DrawingOperation(
                "CLEAR_CANVAS", 0, 0, 0, 0, 
                "#FFFFFF", 0, "current-user"
            );
            listener.onDrawingOperation(operation);
        }
    }

    // Method to apply drawing operations from other clients
    public void applyDrawingOperation(DrawingOperation operation) {
        if (operation != null) {
            operationHistory.add(operation);
            applyOperationWithoutHistory(operation);
        }
    }

    public void setOnDrawingOperationListener(DrawingOperationListener listener) {
        this.listener = listener;
    }

    @FunctionalInterface
    public interface DrawingOperationListener {
        void onDrawingOperation(DrawingOperation operation);
    }
}