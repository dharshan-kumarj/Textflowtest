package com.textflow.server;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.server.Server;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

@ServerEndpoint("/texteditor")
public class WebSocketServer {
    private static final Logger LOGGER = Logger.getLogger(WebSocketServer.class.getName());
    private static final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<String, String> userSessions = new HashMap<>();
    private Server server;
    private static String currentContent = "";

    public void start() {
        server = new Server("localhost", 8025, "/websocket", null, WebSocketServer.class);
        try {
            server.start();
            LOGGER.info("WebSocket server started on ws://localhost:8025/websocket/texteditor");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start WebSocket server", e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop();
            LOGGER.info("WebSocket server stopped");
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        String sessionId = session.getRequestParameterMap().get("sessionId").get(0);
        String username = userSessions.get(sessionId);
        LOGGER.info("New WebSocket connection: " + session.getId() + " for user: " + username);
        sendUserInfo(session, username);
        sendCurrentContent(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        LOGGER.info("Received message from " + session.getId() + ": " + message);
        JSONObject jsonMessage = new JSONObject(message);
        if (jsonMessage.getString("type").equals("content")) {
            currentContent = jsonMessage.getString("content");
            broadcastContent(session);
        }
    }

    private void sendUserInfo(Session session, String username) {
        try {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("type", "userInfo");
            jsonMessage.put("username", username);
            session.getBasicRemote().sendText(jsonMessage.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending user info", e);
        }
    }

    private void sendCurrentContent(Session session) {
        try {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("type", "content");
            jsonMessage.put("content", currentContent);
            session.getBasicRemote().sendText(jsonMessage.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending current content", e);
        }
    }

    private void broadcastContent(Session excludeSession) {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("type", "content");
        jsonMessage.put("content", currentContent);
        String messageString = jsonMessage.toString();
        for (Session s : sessions) {
            if (s.isOpen() && !s.equals(excludeSession)) {
                try {
                    s.getBasicRemote().sendText(messageString);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error broadcasting content", e);
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        LOGGER.info("WebSocket connection closed: " + session.getId());
    }

    @OnError
    public void onError(Throwable error) {
        LOGGER.log(Level.SEVERE, "WebSocket error", error);
    }

    public static void addUserSession(String sessionId, String username) {
        userSessions.put(sessionId, username);
        LOGGER.info("Added user session: " + sessionId + " for user: " + username);
    }
}