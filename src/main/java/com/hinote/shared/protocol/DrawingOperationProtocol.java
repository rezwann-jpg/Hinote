package com.hinote.shared.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DrawingOperationProtocol {
    @JsonProperty("operationType")
    private String operationType;
    
    @JsonProperty("startX")
    private double startX;
    
    @JsonProperty("startY")
    private double startY;
    
    @JsonProperty("endX")
    private double endX;
    
    @JsonProperty("endY")
    private double endY;
    
    @JsonProperty("color")
    private String color;
    
    @JsonProperty("strokeWidth")
    private double strokeWidth;

    @JsonProperty("operationId")
    private String operationId; 

    // Constructors
    public DrawingOperationProtocol() {}

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

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
}