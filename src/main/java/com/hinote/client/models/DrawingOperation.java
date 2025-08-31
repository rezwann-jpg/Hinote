package com.hinote.client.models;

public class DrawingOperation {
    private String operationType; // "DRAW_LINE", "ERASE_LINE", "DRAW_RECTANGLE", "DRAW_CIRCLE", "CLEAR_CANVAS"
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private String color;
    private double strokeWidth;
    private String userId;
    private long timestamp;
    private String operationId; // For better tracking

    public DrawingOperation() {
        this.timestamp = System.currentTimeMillis();
        this.operationId = "op-" + this.timestamp + "-" + System.nanoTime() % 10000;
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

    // Copy constructor for deep copying
    public DrawingOperation(DrawingOperation other) {
        this.operationType = other.operationType;
        this.startX = other.startX;
        this.startY = other.startY;
        this.endX = other.endX;
        this.endY = other.endY;
        this.color = other.color;
        this.strokeWidth = other.strokeWidth;
        this.userId = other.userId;
        this.timestamp = other.timestamp;
        this.operationId = other.operationId;
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

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

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
                ", operationId='" + operationId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DrawingOperation that = (DrawingOperation) obj;
        return operationId != null ? operationId.equals(that.operationId) : super.equals(obj);
    }

    @Override
    public int hashCode() {
        return operationId != null ? operationId.hashCode() : super.hashCode();
    }
}