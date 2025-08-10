package com.hinote.client;

import com.hinote.client.network.WebSocketClientImpl;
import com.hinote.client.network.MessageHandler;

import java.net.URI;

public class HinoteClient {
    public static void main(String[] args) throws Exception {
        MessageHandler handler = new MessageHandler();
        WebSocketClientImpl client = new WebSocketClientImpl(new URI("ws://localhost:8080"), handler);

        client.connectBlocking(); // Wait until connected

        // Send test chat message
        String chatMessageJson = """
        {
          "type": "CHAT",
          "id": "test-123",
          "content": "Hello from client!",
          "timestamp": %d
        }
        """.formatted(System.currentTimeMillis());

        client.sendMessage(chatMessageJson);
    }
}
