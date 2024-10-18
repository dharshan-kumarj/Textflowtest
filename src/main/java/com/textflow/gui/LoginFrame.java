package main.java.com.textflow.gui;

import javax.swing.*;
import java.awt.*;
import main.java.com.textflow.database.DatabaseManager;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private DatabaseManager dbManager;

    public LoginFrame() {
        dbManager = new DatabaseManager();
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
            // TODO: Open main application window or redirect to web editor
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.");
        }
    }

    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame(dbManager);
        registerFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}