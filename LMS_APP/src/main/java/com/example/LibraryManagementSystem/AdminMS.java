package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;

public class AdminMS extends JFrame {
    private static final Color BG_COLOR = new Color(0xaa, 0xaa, 0xaa);
    private AdminDashboard dashboard; // Reference to the parent dashboard

    // Default constructor for standalone operation
    public AdminMS() {
        this(null);
    }

    // Constructor that accepts dashboard reference
    public AdminMS(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        
        setTitle("Manage Students");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel setup
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title at top
        JLabel titleLabel = new JLabel("Manage Students", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Left Panel (Buttons)
        JPanel leftPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        leftPanel.setBackground(BG_COLOR);
        leftPanel.setPreferredSize(new Dimension(200, 0));

        JButton addButton = new JButton("Add Student");
        JButton editButton = new JButton("Edit Student");
        JButton deleteButton = new JButton("Delete Student");

        leftPanel.add(addButton);
        leftPanel.add(editButton);
        leftPanel.add(deleteButton);

        // Right Panel (Table and Fields)
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        // Student Table
        String[] columns = {"Username", "First Name", "Last Name"};
        JTable studentTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Student Details Fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);
        JLabel firstNameLabel = new JLabel("First Name:");
        JTextField firstNameField = new JTextField(20);
        JLabel lastNameLabel = new JLabel("Last Name:");
        JTextField lastNameField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(userLabel, gbc);
        gbc.gridy = 1;
        fieldsPanel.add(userField, gbc);
        gbc.gridy = 0; gbc.gridx = 1;
        fieldsPanel.add(firstNameLabel, gbc);
        gbc.gridy = 1;
        fieldsPanel.add(firstNameField, gbc);
        gbc.gridy = 0; gbc.gridx = 2;
        fieldsPanel.add(lastNameLabel, gbc);
        gbc.gridy = 1;
        fieldsPanel.add(lastNameField, gbc);

        rightPanel.add(fieldsPanel, BorderLayout.SOUTH);

        // Control Buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        controlPanel.setBackground(BG_COLOR);
        JButton backButton = new JButton("Back to Dashboard");
        JButton updateButton = new JButton("Update");
        
        // Set the back button action directly if we have a dashboard reference
        if (dashboard != null) {
            backButton.addActionListener(e -> dashboard.showDashboardPanel());
        }
        
        controlPanel.add(backButton);
        controlPanel.add(updateButton);

        // Combine components
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminMS().setVisible(true));
    }
}