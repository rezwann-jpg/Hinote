package com.hinote.client.ui.components;

import com.hinote.client.models.TextOperation;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class TextComponent {
    private TextOperation textOperation;
    private boolean isSelected = false;

    public TextComponent(TextOperation textOperation) {
        this.textOperation = new TextOperation(textOperation); // Deep copy
    }

    public void render(GraphicsContext gc) {
        gc.save();
        
        // Apply transformations
        gc.translate(textOperation.getX(), textOperation.getY());
        gc.rotate(textOperation.getRotation());
        
        // Set font
        Font font = createFont();
        gc.setFont(font);
        
        // Set color
        gc.setFill(Color.web(textOperation.getColor()));
        
        // Draw text
        gc.fillText(textOperation.getContent(), 0, 0);
        
        // Draw selection border if selected
        if (isSelected) {
            Bounds bounds = getTextBounds(gc, textOperation.getContent());
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1);
            gc.strokeRect(0, bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }
        
        gc.restore();
    }

    private Font createFont() {
        // Convert font weight
        FontWeight fontWeight = "bold".equals(textOperation.getFontWeight()) ? 
            FontWeight.BOLD : FontWeight.NORMAL;
        
        // Convert font style
        FontPosture fontPosture = "italic".equals(textOperation.getFontStyle()) ? 
            FontPosture.ITALIC : FontPosture.REGULAR;
        
        return Font.font(
            textOperation.getFontFamily(),
            fontWeight,
            fontPosture,
            textOperation.getFontSize()
        );
    }

    private Bounds getTextBounds(GraphicsContext gc, String text) {
        // This is a simplified approach - in practice, you might want to use Text node for accurate bounds
        Font font = createFont();
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(font);
        return textNode.getLayoutBounds();
    }

    public boolean containsPoint(double x, double y) {
        // Simple hit detection - in practice, you'd want more accurate bounds
        Bounds bounds = getTextBounds(null, textOperation.getContent());
        double textX = textOperation.getX();
        double textY = textOperation.getY();
        
        return x >= textX && x <= textX + bounds.getWidth() &&
               y >= textY + bounds.getMinY() && y <= textY + bounds.getMaxY();
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public TextOperation getTextOperation() {
        return new TextOperation(textOperation); // Return copy
    }

    public void updateOperation(TextOperation newOperation) {
        this.textOperation = new TextOperation(newOperation); // Deep copy
    }

    public String getTextId() {
        return textOperation.getTextId();
    }
}