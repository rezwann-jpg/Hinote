package com.hinote.server;

import com.hinote.server.handlers.WebSocketHandler;
import com.hinote.server.services.RoomService;
import com.hinote.server.services.SynchronizationService;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class HinoteServer {
    public static void main(String[] args) {
        try {
            // Create services
            RoomService roomService = new RoomService();
            ConcurrentHashMap<String, com.hinote.server.models.ServerRoom> rooms = new ConcurrentHashMap<>();
            SynchronizationService syncService = new SynchronizationService(rooms);
            
            // Create WebSocketHandler
            WebSocketHandler webSocketHandler = new WebSocketHandler(
                new InetSocketAddress("localhost", 8080),
                roomService,
                syncService
            );
            
            // Start the server
            webSocketHandler.start();
            System.out.println("Hinote server started on ws://localhost:8080");
            
            // Keep server running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                try {
                    webSocketHandler.stop();
                } catch (InterruptedException e) {
                    System.err.println("Server shutdown interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }));
            
            // Keep the main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}