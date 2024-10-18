package com.textflow.gui;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import com.textflow.database.DatabaseManager;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private DatabaseManager dbManager;

    public LoginFrame(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        checkDatabaseConnection();
        initComponents();
    }

    private void checkDatabaseConnection() {
        if (!dbManager.isConnected()) {
            JOptionPane.showMessageDialog(this,
                    "Failed to connect to the database. Please check your configuration.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } else {
            String connectionInfo = dbManager.testConnection();
            JOptionPane.showMessageDialog(this,
                    connectionInfo,
                    "Database Connection",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void initComponents() {
        setTitle("TextFlow Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        add(loginButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> openRegisterFrame());
        add(registerButton);

        setLocationRelativeTo(null);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (dbManager.authenticateUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            openTextEditor();
            this.dispose(); // Close the login window
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.");
        }
    }

    private void openTextEditor() {
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:8080/index.html"));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening text editor: " + e.getMessage());
        }
    }

    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame(dbManager);
        registerFrame.setVisible(true);
    }
}