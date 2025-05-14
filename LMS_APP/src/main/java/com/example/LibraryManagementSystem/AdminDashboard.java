package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(0xaa, 0xaa, 0xaa);
    private static final Color CARD_COLOR = new Color(0xef, 0xef, 0xef);
    
    // Card layout for main content
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // Header panel reference
    private JPanel headerPanel;
    
    // Constants for card names
    private static final String DASHBOARD_PANEL = "Dashboard";
    private static final String MANAGE_BOOKS_PANEL = "ManageBooks";
    private static final String BORROWED_BOOKS_PANEL = "BorrowedBooks";
    private static final String MANAGE_STUDENTS_PANEL = "ManageStudents";
    
    // UI component references
    private ManageBooksUI manageBooksUI;
    private AdminBB adminBB;
    private AdminMS adminMS;

    public AdminDashboard() {
        setTitle("Library Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Create header panel
        headerPanel = createHeaderPanel();
        
        // Create content panel with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Create the dashboard panel (with header)
        JPanel dashboardPanelWithHeader = new JPanel(new BorderLayout());
        dashboardPanelWithHeader.setBackground(BACKGROUND_COLOR);
        dashboardPanelWithHeader.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add header to the dashboard panel
        dashboardPanelWithHeader.add(headerPanel, BorderLayout.NORTH);
        
        // Create and add dashboard content
        JPanel dashboardContent = createDashboardPanel();
        dashboardPanelWithHeader.add(dashboardContent, BorderLayout.CENTER);
        
        // Add dashboard panel to content panel
        contentPanel.add(dashboardPanelWithHeader, DASHBOARD_PANEL);
        
        // Initialize other UI components
        initializeUIComponents();
        
        // Add content panel to main panel
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Show dashboard card initially
        cardLayout.show(contentPanel, DASHBOARD_PANEL);

        add(mainPanel);
    }
    
    private void initializeUIComponents() {
        // Initialize ManageBooksUI
        manageBooksUI = new ManageBooksUI();
        JPanel manageBooksPanel = manageBooksUI.getContentPanel();
        addBackButtonToManageBooks(manageBooksPanel);
        contentPanel.add(manageBooksPanel, MANAGE_BOOKS_PANEL);
        
        // Initialize AdminBB (Borrowed Books)
        adminBB = new AdminBB();
        JPanel borrowedBooksPanel = getBorrowedBooksPanel(); 
        contentPanel.add(borrowedBooksPanel, BORROWED_BOOKS_PANEL);
        
        // Initialize AdminMS (Manage Students) - Pass this dashboard instance
        adminMS = new AdminMS(this);
        JPanel manageStudentsPanel = getManageStudentsPanel();
        contentPanel.add(manageStudentsPanel, MANAGE_STUDENTS_PANEL);
    }
    
    private JPanel getBorrowedBooksPanel() {
        // Get the content from AdminBB
        JPanel mainPanel = (JPanel) adminBB.getContentPane().getComponent(0);
        
        // Add a title at the top
        JLabel titleLabel = new JLabel("Borrowed Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        // Find and modify the results panel to include the title
        Component[] components = mainPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                // Add the title to the first panel we find (or we could use more specific criteria)
                if (panel.getLayout() instanceof BorderLayout) {
                    panel.add(titleLabel, BorderLayout.NORTH);
                    break;
                }
            }
        }
        
        // Find and modify the back button
        for (Component component : mainPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                modifyBackButton(panel);
            }
        }
        
        return mainPanel;
    }
    
    private JPanel getManageStudentsPanel() {
        // Get the content from AdminMS
        JPanel mainPanel = (JPanel) adminMS.getContentPane().getComponent(0);
        
        // Find and modify the back button 
        JPanel controlPanel = (JPanel) mainPanel.getComponent(2); // South component
        modifyBackButton(controlPanel);
        
        return mainPanel;
    }
    
    private void modifyBackButton(Container container) {
        Component[] components = container.getComponents();
        
        for (Component component : components) {
            if (component instanceof JButton && 
                (((JButton) component).getText().equals("Back") ||
                 ((JButton) component).getText().equals("Back to Dashboard"))) {
                JButton backButton = (JButton) component;
                
                // Remove existing action listeners
                for (java.awt.event.ActionListener al : backButton.getActionListeners()) {
                    backButton.removeActionListener(al);
                }
                
                // Add new action listener to show dashboard
                backButton.setText("Back to Dashboard");
                backButton.addActionListener(e -> showDashboardPanel());
                return;
            } else if (component instanceof Container) {
                // Recursively search in this container
                modifyBackButton((Container) component);
            }
        }
    }
    
    private void addBackButtonToManageBooks(JPanel manageBooksPanel) {
        // The structure from ManageBooksUI
        JPanel bottomPanel = (JPanel) manageBooksPanel.getComponent(3); // Bottom panel
        JPanel buttonPanel = (JPanel) bottomPanel.getComponent(2); // Lower right panel with buttons
        
        // Check if "Back to Dashboard" button already exists
        boolean backButtonExists = false;
        Component[] components = buttonPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton && ((JButton) comp).getText().equals("Back to Dashboard")) {
                JButton backButton = (JButton) comp;
                // Update action listener
                for (java.awt.event.ActionListener al : backButton.getActionListeners()) {
                    backButton.removeActionListener(al);
                }
                backButton.addActionListener(e -> showDashboardPanel());
                backButtonExists = true;
                break;
            }
        }
        
        // Add button if it doesn't exist
        if (!backButtonExists) {
            JButton backToDashboardButton = new JButton("Back to Dashboard");
            backToDashboardButton.addActionListener(e -> showDashboardPanel());
            buttonPanel.add(backToDashboardButton);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerPanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Set preferred size to make the title centered
        titleLabel.setPreferredSize(new Dimension(500, 30));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(100, 30));
        logoutButton.addActionListener(e -> System.exit(0));

        headerPanel.add(titleLabel);
        headerPanel.add(logoutButton);

        return headerPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        dashboardPanel.setBackground(BACKGROUND_COLOR);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 400, 50));

        String[] cardLabels = {
                "Manage Books",
                "Borrowed Books",
                "Returned Books",
                "Manage Students"
        };

        for (String label : cardLabels) {
            JPanel card = createDashboardCard(label);

            if (label.equals("Manage Books")) {
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showManageBooksPanel();
                    }
                });
            } else if (label.equals("Borrowed Books")) {
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showBorrowedBooksPanel();
                    }
                });
            } else if (label.equals("Manage Students")) {
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showManageStudentsPanel();
                    }
                });
            }

            dashboardPanel.add(card);
        }

        return dashboardPanel;
    }

    private JPanel createDashboardCard(String text) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Add hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(CARD_COLOR.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(CARD_COLOR);
            }
        });

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.BELOW_BASELINE;
        card.add(label, gbc);

        return card;
    }
    
    // Navigation methods
    public void showDashboardPanel() {
        setTitle("Library Management System - Admin Dashboard");
        cardLayout.show(contentPanel, DASHBOARD_PANEL);
    }
    
    public void showManageBooksPanel() {
        setTitle("Library Management System - Manage Books");
        manageBooksUI.viewAvailableBooks(); // Refresh the books data
        cardLayout.show(contentPanel, MANAGE_BOOKS_PANEL);
    }
    
    public void showBorrowedBooksPanel() {
        setTitle("Library Management System - Borrowed Books");
        cardLayout.show(contentPanel, BORROWED_BOOKS_PANEL);
    }
    
    public void showManageStudentsPanel() {
        setTitle("Library Management System - Manage Students");
        cardLayout.show(contentPanel, MANAGE_STUDENTS_PANEL);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setVisible(true);
        });
    }
}