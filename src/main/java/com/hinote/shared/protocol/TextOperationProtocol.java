package com.hinote.shared.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TextOperationProtocol {
    @JsonProperty("operationType")
    private TextOperationType operationType;
    
    @JsonProperty("textId")
    private String textId;
    
    @JsonProperty("x")
    private double x;
    
    @JsonProperty("y")
    private double y;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("fontSize")
    private double fontSize;
    
    @JsonProperty("fontFamily")
    private String fontFamily;
    
    @JsonProperty("fontWeight")
    private String fontWeight;
    
    @JsonProperty("fontStyle")
    private String fontStyle;
    
    @JsonProperty("color")
    private String color;
    
    @JsonProperty("rotation")
    private double rotation;
    
    @JsonProperty("width")
    private double width;
    
    @JsonProperty("height")
    private double height;
    
    public enum TextOperationType {
        CREATE_TEXT,
        EDIT_TEXT,
        MOVE_TEXT,
        DELETE_TEXT,
        STYLE_TEXT,
        RESIZE_TEXT
    }
    
    public TextOperationProtocol() {}
    
    public TextOperationProtocol(TextOperationType operationType, String textId, double x, double y, String content) {
        this.operationType = operationType;
        this.textId = textId;
        this.x = x;
        this.y = y;
        this.content = content;
        this.fontSize = 12.0;
        this.fontFamily = "Arial";
        this.fontWeight = "normal";
        this.fontStyle = "normal";
        this.color = "#000000";
        this.rotation = 0.0;
    }
    
    public static TextOperationProtocol createText(String textId, double x, double y, String content) {
        return new TextOperationProtocol(TextOperationType.CREATE_TEXT, textId, x, y, content);
    }
    
    public static TextOperationProtocol editText(String textId, String content) {
        TextOperationProtocol protocol = new TextOperationProtocol();
        protocol.setOperationType(TextOperationType.EDIT_TEXT);
        protocol.setTextId(textId);
        protocol.setContent(content);
        return protocol;
    }
    
    public static TextOperationProtocol moveText(String textId, double x, double y) {
        TextOperationProtocol protocol = new TextOperationProtocol();
        protocol.setOperationType(TextOperationType.MOVE_TEXT);
        protocol.setTextId(textId);
        protocol.setX(x);
        protocol.setY(y);
        return protocol;
    }
    
    public static TextOperationProtocol deleteText(String textId) {
        TextOperationProtocol protocol = new TextOperationProtocol();
        protocol.setOperationType(TextOperationType.DELETE_TEXT);
        protocol.setTextId(textId);
        return protocol;
    }

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
}