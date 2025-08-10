package com.hinote.shared.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatMessageProtocol {
    @JsonProperty("content")
    private String content;

    @JsonProperty("isSystemMessage")
    private boolean isSystemMessage;
    
    public ChatMessageProtocol() {}
    
    public ChatMessageProtocol(String content, boolean isSystemMessage) {
        this.content = content;
        this.isSystemMessage = isSystemMessage;
    }
    
    public static ChatMessageProtocol userMessage(String content) {
        return new ChatMessageProtocol(content, false);
    }
    
    public static ChatMessageProtocol systemMessage(String content) {
        return new ChatMessageProtocol(content, true);
    }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public boolean isSystemMessage() { return isSystemMessage; }
    public void setSystemMessage(boolean systemMessage) { this.isSystemMessage = systemMessage; }
}
