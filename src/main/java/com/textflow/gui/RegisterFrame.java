package main.java.com.textflow.gui;

import javax.swing.*;
import java.awt.*;
import main.java.com.textflow.database.DatabaseManager;

public class RegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton registerButton;
    private DatabaseManager dbManager;

    public RegisterFrame(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initComponents();
    }

    private void initComponents() {
        setTitle("TextFlow Registration");
        setSize(300, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> register());
        add(registerButton);

        setLocationRelativeTo(null);
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        if (dbManager.registerUser(username, password, email)) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Please try again.");
        }
    }
}