package com.hinote.client;

import com.hinote.client.models.ChatMessage;
import com.hinote.client.network.ConnectionManager;
import com.hinote.client.network.MessageHandler;
import com.hinote.client.ui.MainController;
import com.hinote.shared.protocol.ChatMessageProtocol;
import com.hinote.shared.protocol.Message;
import com.hinote.shared.protocol.MessageType;
import com.hinote.shared.utils.IdGenerator;
import com.hinote.shared.utils.JsonUtil;

public class HinoteClient {
    public static void main(String[] args) {
        try {
            MainController mockController = new MockMainController();
            
            MessageHandler handler = new MessageHandler(mockController);
            
            String serverUrl = "ws://localhost:8080";
            ConnectionManager connectionManager = new ConnectionManager(serverUrl, handler.handleMessage());
            
            // Wait a bit for connection to establish
            Thread.sleep(2000);
            
            if (connectionManager.isConnected()) {
                System.out.println("Connected to server!");
                
                // Send test chat message using proper Message object
                Message message = new Message(
                    MessageType.CHAT_MESSAGE,
                    IdGenerator.generateUniqueId(),
                    "test-room", // room ID
                    "test-user", // user ID
                    "TestUser",  // username
                    JsonUtil.toJsonNode(ChatMessageProtocol.userMessage("Hello from client!"))
                );
                
                connectionManager.sendMessage(message);
                System.out.println("Test message sent!");
            } else {
                System.out.println("Failed to connect to server");
            }
            
            // Keep the client running for a while to see responses
            Thread.sleep(10000);
            
            // Clean shutdown
            connectionManager.disconnect();
            
        } catch (InterruptedException e) {
            System.err.println("Client operation interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Simple mock controller for testing
    static class MockMainController extends MainController {
        @Override
        public void addChatMessage(ChatMessage message) {
            System.out.println("Received chat message: " + message.getContent());
        }
        
        @Override
        public void showSystemMessage(String content) {
            System.out.println("System message: " + content);
        }
        
        @Override
        public void showErrorMessage(String content) {
            System.out.println("Error message: " + content);
        }
    }
}