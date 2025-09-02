package com.hinote.client.models;

public class TextOperation {
    private String textId;
    private TextOperationType operationType; // CREATE_TEXT, EDIT_TEXT, MOVE_TEXT, DELETE_TEXT, STYLE_TEXT
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
    private String userId;
    private long timestamp;
    private String operationId;

    public enum TextOperationType {
        CREATE_TEXT,
        EDIT_TEXT,
        MOVE_TEXT,
        DELETE_TEXT,
        STYLE_TEXT,
        RESIZE_TEXT
    }

    public TextOperation() {
        this.timestamp = System.currentTimeMillis();
        this.fontSize = 16.0;
        this.fontFamily = "Arial";
        this.fontWeight = "normal";
        this.fontStyle = "normal";
        this.color = "#000000";
        this.rotation = 0.0;
    }

    // Constructor for creating new text
    public TextOperation(String textId, double x, double y, String content) {
        this();
        this.textId = textId;
        this.operationId = "textop-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        this.operationType = TextOperationType.CREATE_TEXT;
        this.x = x;
        this.y = y;
        this.content = content != null ? content : "Double-click to edit";
    }

    // Copy constructor
    public TextOperation(TextOperation other) {
        this.textId = other.textId;
        this.operationType = other.operationType;
        this.x = other.x;
        this.y = other.y;
        this.content = other.content;
        this.fontSize = other.fontSize;
        this.fontFamily = other.fontFamily;
        this.fontWeight = other.fontWeight;
        this.fontStyle = other.fontStyle;
        this.color = other.color;
        this.rotation = other.rotation;
        this.width = other.width;
        this.height = other.height;
        this.userId = other.userId;
        this.timestamp = other.timestamp;
        this.operationId = other.operationId;
    }

    // Getters and setters
    public String getTextId() { return textId; }
    public void setTextId(String textId) { this.textId = textId; }

    public TextOperationType getOperationType() { return operationType; }
    public void setOperationType(TextOperationType operationType) { this.operationType = operationType; }

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

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

    @Override
    public String toString() {
        return "TextOperation{" +
                "textId='" + textId + '\'' +
                ", operationType=" + operationType +
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TextOperation that = (TextOperation) obj;
        return operationId != null ? operationId.equals(that.operationId) : super.equals(obj);
    }

    @Override
    public int hashCode() {
        return operationId != null ? operationId.hashCode() : super.hashCode();
    }
}