package com.example.LibraryManagementSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JFrame {
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 500;
    private AuthFunction authFunctions = new AuthFunction(this);
    private JPanel mainPanel;

    public LoginPage() {
        // Set up the frame for full screen with decorations
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login System");

        // Create main panel with GridBagLayout to center our login panel
        mainPanel = new JPanel(new GridBagLayout());
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
    
    // Method to create and return the main login content panel
    public static JPanel createLoginContent(JFrame parentFrame) {
        // Create main panel with GridBagLayout to center our login panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0xaa, 0xaa, 0xaa)); // Light background color

        // Create the fixed-size login panel - passing true to indicate it's coming from RegisterPage
        JPanel loginPanel = createLoginPanel(parentFrame, parentFrame instanceof RegisterPage);

        // Add login panel to center of main panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(loginPanel, gbc);
        
        return mainPanel;
    }

    // This is now static and takes a parent frame parameter and a showBackButton flag
    private static JPanel createLoginPanel(JFrame parentFrame, boolean showBackButton) {
        // Create the login panel with fixed size
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

        // Title label
        JLabel titleLabel = new JLabel("Log-in", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.insets = new Insets(0, 5, 10, 5);
        loginPanel.add(titleLabel, gbc);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(50, 5, 10, 5);
        loginPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(15);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(10, 5, 10, 5);
        usernameField.setPreferredSize(new Dimension(100, 30));
        loginPanel.add(usernameField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.ABOVE_BASELINE;
        gbc.insets = new Insets(35, 5, 10, 5);
        loginPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(10, 5, 10, 5);
        passwordField.setPreferredSize(new Dimension(100, 30));
        loginPanel.add(passwordField, gbc);

        // Button Panel for login and back buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(0xDADADA));
        
        // Back Button (only shown when coming from RegisterPage)
        if (showBackButton) {
            JButton backButton = new JButton("Back to Register");
            backButton.setFont(new Font("Arial", Font.PLAIN, 14));
            backButton.setPreferredSize(new Dimension(140, 35));
            backButton.addActionListener(e -> {
                // Only if parent is a RegisterPage instance
                if (parentFrame instanceof RegisterPage) {
                    RegisterPage registerPage = (RegisterPage) parentFrame;
                    registerPage.switchToRegisterPanel();
                }
            });
            buttonPanel.add(backButton);
        }

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        
        // Handle login action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                // Create a new AuthFunction if this is used from RegisterPage
                if (parentFrame instanceof LoginPage) {
                    ((LoginPage) parentFrame).getAuthFunction().loginFunction(username, password);
                } else {
                    AuthFunction authFunction = new AuthFunction(parentFrame);
                    authFunction.loginFunction(username, password);
                }
            }
        });
        buttonPanel.add(loginButton);
        
        // Add button panel to login panel
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(50, 5, 10, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(buttonPanel, gbc);

        return loginPanel;
    }
    
    // Original method for backward compatibility
    private JPanel createLoginPanel() {
        return createLoginPanel(this, false); // Normal login page with no back button
    }

    public AuthFunction getAuthFunction() {
        return authFunctions;
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