package com.hinote.client.models;

public class DrawingOperation {
    private String operationType; // "DRAW_LINE", "ERASE_LINE", etc.
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private String color;
    private double strokeWidth;
    private String userId;
    private long timestamp;

    public DrawingOperation() {
        this.timestamp = System.currentTimeMillis();
    }

    public DrawingOperation(String operationType, double startX, double startY, 
                          double endX, double endY, String color, double strokeWidth, String userId) {
        this();
        this.operationType = operationType;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.userId = userId;
    }

    // Getters and setters
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public double getStartX() { return startX; }
    public void setStartX(double startX) { this.startX = startX; }

    public double getStartY() { return startY; }
    public void setStartY(double startY) { this.startY = startY; }

    public double getEndX() { return endX; }
    public void setEndX(double endX) { this.endX = endX; }

    public double getEndY() { return endY; }
    public void setEndY(double endY) { this.endY = endY; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(double strokeWidth) { this.strokeWidth = strokeWidth; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "DrawingOperation{" +
                "operationType='" + operationType + '\'' +
                ", startX=" + startX +
                ", startY=" + startY +
                ", endX=" + endX +
                ", endY=" + endY +
                ", color='" + color + '\'' +
                ", strokeWidth=" + strokeWidth +
                ", userId='" + userId + '\'' +
                '}';
    }
}