package com.hinote.client.models;

public class TextOperation {
    private TextOperationType operationType; // Changed from String to TextOperationType
    private String textId;
    private double x;
    private double y;
    private String content;
    private double fontSize;
    private String fontFamily;
    private String fontWeight;
    private String fontStyle;
    private String color;
    private double rotation;
    private double width;
    private double height;

    // Default constructor
    public TextOperation() {
        this.fontSize = 12.0;
        this.fontFamily = "Arial";
        this.fontWeight = "normal";
        this.fontStyle = "normal";
        this.color = "#000000";
        this.rotation = 0.0;
    }

    // Constructor with basic parameters
    public TextOperation(String textId, TextOperationType operationType, double x, double y, String content,
                        double fontSize, String fontFamily, String fontWeight, String fontStyle,
                        String color, double rotation) {
        this.textId = textId;
        this.operationType = operationType;
        this.x = x;
        this.y = y;
        this.content = content;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.fontWeight = fontWeight;
        this.fontStyle = fontStyle;
        this.color = color;
        this.rotation = rotation;
        this.width = 0.0;
        this.height = 0.0;
    }

    // Constructor with all parameters including width and height
    public TextOperation(String textId, TextOperationType operationType, double x, double y, String content,
                        double fontSize, String fontFamily, String fontWeight, String fontStyle,
                        String color, double rotation, double width, double height) {
        this.textId = textId;
        this.operationType = operationType;
        this.x = x;
        this.y = y;
        this.content = content;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.fontWeight = fontWeight;
        this.fontStyle = fontStyle;
        this.color = color;
        this.rotation = rotation;
        this.width = width;
        this.height = height;
    }

    // Getters and setters
    public TextOperationType getOperationType() { return operationType; }
    public void setOperationType(TextOperationType operationType) { this.operationType = operationType; }
    
    public String getTextId() { return textId; }
    public void setTextId(String textId) { this.textId = textId; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public double getFontSize() { return fontSize; }
    public void setFontSize(double fontSize) { this.fontSize = fontSize; }
    
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    
    public String getFontWeight() { return fontWeight; }
    public void setFontWeight(String fontWeight) { this.fontWeight = fontWeight; }
    
    public String getFontStyle() { return fontStyle; }
    public void setFontStyle(String fontStyle) { this.fontStyle = fontStyle; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public double getRotation() { return rotation; }
    public void setRotation(double rotation) { this.rotation = rotation; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    @Override
    public String toString() {
        return "TextOperation{" +
                "operationType=" + operationType +
                ", textId='" + textId + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", content='" + content + '\'' +
                ", fontSize=" + fontSize +
                ", fontFamily='" + fontFamily + '\'' +
                ", fontWeight='" + fontWeight + '\'' +
                ", fontStyle='" + fontStyle + '\'' +
                ", color='" + color + '\'' +
                ", rotation=" + rotation +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}