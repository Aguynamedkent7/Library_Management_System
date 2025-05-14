package com.example.LibraryManagementSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JFrame {
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 500;

    public LoginPage() {
        // Set up the frame for full screen with decorations
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login System");

        // Create main panel with GridBagLayout to center our login panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0xaa, 0xaa, 0xaa)); // Light background color

        // Create the fixed-size login panel
        JPanel loginPanel = createLoginPanel();

        // Add login panel to center of main panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(loginPanel, gbc);

        // Add main panel to frame
        add(mainPanel);
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(0, 20, 20, 20)
        ));
        loginPanel.setBackground(new Color(0xDADADA));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title label (centered)
        JLabel titleLabel = new JLabel("Log-in", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 30, 5);
        loginPanel.add(titleLabel, gbc);

        // Username (centered)
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 5, 5);
        loginPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(15);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 20, 5);
        usernameField.setPreferredSize(new Dimension(200, 30));
        loginPanel.add(usernameField, gbc);

        // Password (centered)
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        loginPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 30, 5);
        passwordField.setPreferredSize(new Dimension(200, 30));
        loginPanel.add(passwordField, gbc);

        // Button panel (centered)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(0xDADADA));

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            JOptionPane.showMessageDialog(LoginPage.this,
                    "Login attempted with:\nUsername: " + username);
        });

        // Create Account button
        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.setPreferredSize(new Dimension(140, 35));
        createAccountButton.addActionListener(e -> {
            // Add navigation to register page here
            JOptionPane.showMessageDialog(LoginPage.this,
                    "Redirecting to account creation...");
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(createAccountButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 20, 5);
        loginPanel.add(buttonPanel, gbc);

        return loginPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginPage().setVisible(true);
            }
        });
    }
}