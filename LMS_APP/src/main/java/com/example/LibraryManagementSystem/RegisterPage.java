package com.example.LibraryManagementSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPage extends JFrame {
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 500;

    public RegisterPage() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Registration System");

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0xaa, 0xaa, 0xaa));

        JPanel registerPanel = createRegisterPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(registerPanel, gbc);

        add(mainPanel);
    }

    private JPanel createRegisterPanel() {
        JPanel registerPanel = new JPanel(new GridBagLayout());
        registerPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        registerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        registerPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 0, 15, 0);

        // Title
        JLabel titleLabel = new JLabel("Register", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5,0,10,0);
        registerPanel.add(titleLabel, gbc);

        // First Name
        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        registerPanel.add(firstNameLabel, gbc);

        JTextField firstNameField = new JTextField();
        firstNameField.setPreferredSize(new Dimension(250, 30));
        gbc.gridy = 2;
        registerPanel.add(firstNameField, gbc);

        // Last Name
        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 3;
        registerPanel.add(lastNameLabel, gbc);

        JTextField lastNameField = new JTextField();
        lastNameField.setPreferredSize(new Dimension(250, 30));
        gbc.gridy = 4;
        registerPanel.add(lastNameField, gbc);

        // Username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 5;
        registerPanel.add(userLabel, gbc);

        JTextField userField = new JTextField();
        userField.setPreferredSize(new Dimension(250, 30));
        gbc.gridy = 6;
        registerPanel.add(userField, gbc);

        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 7;
        registerPanel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(250, 30));
        gbc.gridy = 8;
        registerPanel.add(passField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        // Back Button
        JButton backButton = new JButton("Back to Login");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.setPreferredSize(new Dimension(140, 35));
        backButton.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });

        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(120, 35));
        registerButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String username = userField.getText();
            String password = new String(passField.getPassword());

            JOptionPane.showMessageDialog(RegisterPage.this,
                    "Registration Details:\nName: " + firstName + " " + lastName
                            + "\nUsername: " + username);
        });

        buttonPanel.add(backButton);
        buttonPanel.add(registerButton);

        gbc.gridy = 9;
        gbc.insets = new Insets(20, 0, 0, 0);
        registerPanel.add(buttonPanel, gbc);

        return registerPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterPage().setVisible(true));
    }
}