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
    private static final String LIBRARY_INVENTORY_PANEL = "LibraryInventory";
    
    // UI component references
    private ManageBooksUI manageBooksUI;
    private LibraryInventoryUI libraryInventoryUI;
    private BorrowedBooksUI borrowedBooksUI;

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
        
        // Create a table for borrowed books
        JTable borrowedBooksTable = new JTable();
        borrowedBooksTable.setDefaultEditor(Object.class, null);
        borrowedBooksTable.getTableHeader().setReorderingAllowed(false);
        borrowedBooksTable.setDragEnabled(false);
        borrowedBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create a header label for borrowed books
        JLabel borrowedBooksHeaderLabel = new JLabel("All Borrowers", SwingConstants.CENTER);
        borrowedBooksHeaderLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Initialize BorrowedBooksUI with the table and header
        borrowedBooksUI = new BorrowedBooksUI(borrowedBooksTable, borrowedBooksHeaderLabel);
        JPanel borrowedBooksPanel = createBorrowedBooksPanel(borrowedBooksUI);
        contentPanel.add(borrowedBooksPanel, BORROWED_BOOKS_PANEL);
        
        // Initialize LibraryInventoryUI
        libraryInventoryUI = new LibraryInventoryUI();
        // Set back button to return to dashboard
        libraryInventoryUI.setBackButtonAction(e -> showDashboardPanel());
        JPanel libraryInventoryPanel = libraryInventoryUI.getMainPanel();
        contentPanel.add(libraryInventoryPanel, LIBRARY_INVENTORY_PANEL);
    }
    
    private JPanel createBorrowedBooksPanel(BorrowedBooksUI borrowedBooksUI) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add header to panel
        mainPanel.add(borrowedBooksUI.getHeaderLabel(), BorderLayout.NORTH);
        
        // Get content panel from BorrowedBooksUI
        JPanel contentPanel = borrowedBooksUI.getContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Create bottom panel for back button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back to Dashboard");
        JButton returnButton = new JButton("Return Book");
        backButton.addActionListener(e -> showDashboardPanel());
        returnButton.addActionListener(e -> { borrowedBooksUI.getController().showReturnDialog();});
        bottomPanel.add(backButton);
        bottomPanel.add(returnButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JPanel getLibraryInventoryUIPanel() {
        return libraryInventoryUI.getMainPanel();
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
                "Scan to return a book",
                "Library Inventory"
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
            } else if (label.equals("Library Inventory")) {
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showLibraryInventoryPanel();
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
        borrowedBooksUI.getController().loadBorrowedBooks();
    }
    
    public void showLibraryInventoryPanel() {
        setTitle("Library Management System - Library Inventory");
        cardLayout.show(contentPanel, LIBRARY_INVENTORY_PANEL);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setVisible(true);
        });
    }
}