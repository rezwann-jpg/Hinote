package com.hinote.server.handlers;

import com.hinote.server.handlers.MessageRouter;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;

public class WebSocketHandler extends WebSocketServer {

    private final MessageRouter messageRouter;

    public WebSocketHandler(InetSocketAddress address) {
        super(address);
        this.messageRouter = new MessageRouter();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[Server] Connection opened: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("[Server] Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        messageRouter.route(conn, message, this);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("[Server] Started successfully!");
    }
}
