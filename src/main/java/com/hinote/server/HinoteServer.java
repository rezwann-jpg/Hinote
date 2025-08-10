package com.hinote.server;

import com.hinote.server.handlers.WebSocketHandler;
import java.net.InetSocketAddress;

public class HinoteServer {
    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        WebSocketHandler server = new WebSocketHandler(address);
        server.start();
        System.out.println("Hinote Server started on ws://" + address.getHostName() + ":" + address.getPort());
    }
}
