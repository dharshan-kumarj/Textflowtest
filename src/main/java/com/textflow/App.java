package com.textflow;

import com.sun.net.httpserver.*;
import com.textflow.database.DatabaseManager;
import com.textflow.server.WebSocketServer;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private DatabaseManager dbManager;
    private WebSocketServer webSocketServer;
    private HttpServer httpServer;
    private Map<String, String> sessions = new HashMap<>();

    public App() {
        initializeComponents();
    }

    private void initializeComponents() {
        LOGGER.info("Initializing components...");
        dbManager = new DatabaseManager();
        webSocketServer = new WebSocketServer();
        LOGGER.info("Components initialized.");
    }

    private void start() {
        LOGGER.info("Starting application...");
        if (dbManager.isConnected()) {
            startWebSocketServer();
            startHttpServer();
        } else {
            LOGGER.severe("Failed to connect to the database. Exiting.");
            System.exit(1);
        }
    }

    private void startWebSocketServer() {
        try {
            LOGGER.info("Starting WebSocket server...");
            webSocketServer.start();
            LOGGER.info("WebSocket server started successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start WebSocket server", e);
            System.exit(1);
        }
    }

    private void startHttpServer() {
        try {
            LOGGER.info("Starting HTTP server...");
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            httpServer.createContext("/", new FileHandler());
            httpServer.createContext("/login", new LoginHandler());
            httpServer.setExecutor(null);
            httpServer.start();
            LOGGER.info("HTTP Server started on http://localhost:8080/");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start HTTP server", e);
        }
    }

    class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String root = "src/main/resources/web";
            String path = exchange.getRequestURI().getPath();
            String filePath = root + (path.equals("/") ? "/index.html" : path);

            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
                exchange.sendResponseHeaders(200, fileBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }
                LOGGER.info("Served file: " + filePath);
            } catch (IOException e) {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                LOGGER.warning("File not found: " + filePath);
            }
        }
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        requestBody.append(line);
                    }
                    JSONObject jsonRequest = new JSONObject(requestBody.toString());
                    String username = jsonRequest.getString("username");
                    String password = jsonRequest.getString("password");

                    if (dbManager.authenticateUser(username, password)) {
                        String sessionId = UUID.randomUUID().toString();
                        sessions.put(sessionId, username);
                        WebSocketServer.addUserSession(sessionId, username);
                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put("success", true);
                        jsonResponse.put("sessionId", sessionId);
                        sendResponse(exchange, 200, jsonResponse.toString());
                        LOGGER.info("User logged in: " + username);
                    } else {
                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put("success", false);
                        jsonResponse.put("message", "Invalid credentials");
                        sendResponse(exchange, 401, jsonResponse.toString());
                        LOGGER.warning("Failed login attempt for username: " + username);
                    }
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    public static void main(String[] args) {
        LOGGER.info("Application starting...");
        App app = new App();
        app.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down application...");
            app.webSocketServer.stop();
            app.httpServer.stop(0);
            LOGGER.info("Application shut down complete.");
        }));
    }
}