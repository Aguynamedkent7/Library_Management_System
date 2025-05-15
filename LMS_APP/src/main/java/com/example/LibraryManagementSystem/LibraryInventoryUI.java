package com.example.LibraryManagementSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class LibraryInventoryUI extends JPanel {
    private LibraryInventoryFunctionality controller = new LibraryInventoryFunctionality(this);
    private JTable inventoryTable;
    private JLabel headerLabel;
    private Font headerFont = new Font("Arial", Font.BOLD, 20);
    private JPanel mainPanel;
    private JScrollPane tableScrollPane;
    private JLabel qrCodeLabel; // Added QR code label
    private LibraryInventoryFunctionality functionality;

    public LibraryInventoryUI() {
        initializeUI();
    }

    private void initializeUI() {

        functionality = new LibraryInventoryFunctionality(this);
        // Set up the main panel with BorderLayout
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header for the inventory display
        headerLabel = new JLabel("Library Inventory", SwingConstants.CENTER);
        headerLabel.setFont(headerFont);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Create the table
        inventoryTable = new JTable();
        inventoryTable.setDefaultEditor(Object.class, null); // Make table non-editable
        inventoryTable.getTableHeader().setReorderingAllowed(false); // Prevent column reordering
        inventoryTable.setDragEnabled(false); // Prevent row reordering
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set up the table columns - FIXED: Added "Available Copies" column to match data
        String[] columnNames = {"Book Copy ID", "Title", "Author", "Genre", "Publisher", "Date Published"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        inventoryTable.setModel(model);

        controller.loadAllBooks();
        
        // Add table to a scroll pane
        tableScrollPane = new JScrollPane(inventoryTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Create a panel for the right side
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        // QR Code display (top-right)
        qrCodeLabel = new JLabel();
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBorder(BorderFactory.createTitledBorder("Book QR Code"));
        qrPanel.add(qrCodeLabel, BorderLayout.CENTER);
        qrPanel.setPreferredSize(new Dimension(300, 300));
        
        // Add save QR button inside QR panel
        JPanel qrButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveQRButton = new JButton("Save QR Code");
        saveQRButton.addActionListener(e -> {
            if (qrCodeLabel.getIcon() != null) {
                controller.saveQRCodeToFile(qrCodeLabel, this);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No QR code to save! Generate a QR code first.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        qrButtonPanel.add(saveQRButton);
        qrPanel.add(qrButtonPanel, BorderLayout.SOUTH);
        
        rightPanel.add(qrPanel, BorderLayout.NORTH);
        
        // Control panel for the bottom-right section
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Actions"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        controlPanel.setLayout(new GridLayout(4, 1, 0, 10)); // 3 rows, 1 column, 10px vertical gap
        
        JButton generateQRButton = new JButton("Generate QR Code");
        JButton generateAllQRButton = new JButton("Generate All QR Codes");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back");

        generateAllQRButton.addActionListener(e -> handleGenerateAllQRCodes());

        
        // Add QR code generation action
        generateQRButton.addActionListener(e -> {
            int selectedRow = inventoryTable.getSelectedRow();
            if (selectedRow >= 0) {
                controller.generateQRCode(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a book to generate QR code!", 
                    "Selection Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        refreshButton.addActionListener(e -> controller.loadAllBooks());
        
        // Add buttons to the control panel
        controlPanel.add(generateQRButton);
        controlPanel.add(generateAllQRButton);
        controlPanel.add(refreshButton);
        controlPanel.add(backButton);
        
        // Add control panel to the bottom of the right panel
        rightPanel.add(controlPanel, BorderLayout.CENTER);
        
        // Add right panel to the main panel
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        // Add the main panel to this JPanel
        setLayout(new BorderLayout());
        add(mainPanel);
    }
    
    /**
     * Generates a QR code for the selected book
     */

    private void handleGenerateAllQRCodes() {
        // Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(
                mainPanel,
                "This will generate QR codes for ALL books.\nContinue?",
                "Confirm Bulk Generation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Run in background thread to prevent UI freeze
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    functionality.generateAllQRCodes();
                    return null;
                }

                @Override
                protected void done() {
                    // Any post-processing
                }
            }.execute();
        }
    }

    public Object[] getBookAtRow(int row) {
        DefaultTableModel model = (DefaultTableModel) inventoryTable.getModel();
        Object[] bookData = new Object[6];
        
        // FIXED: Get the actual column count from the model instead of hardcoding
        int columnCount = model.getColumnCount();
        for (int i = 0; i < columnCount && i < bookData.length; i++) {
            bookData[i] = model.getValueAt(row, i);
        }
        return bookData;
    }
    
    /**
     * Displays the QR code in the UI
     */
    public void displayQRCode(ImageIcon qrImage) {
        qrCodeLabel.setIcon(qrImage);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    /**
     * Updates the back button's action
     */
    public void setBackButtonAction(java.awt.event.ActionListener action) {
        JPanel rightPanel = (JPanel) mainPanel.getComponent(2); // Get the right panel
        JPanel controlPanel = (JPanel) rightPanel.getComponent(1); // Get the control panel with buttons
        
        // The third component (index 2) in the control panel is the Back button
        JButton backButton = (JButton) controlPanel.getComponent(2);
        
        // Remove existing action listeners
        for (java.awt.event.ActionListener al : backButton.getActionListeners()) {
            backButton.removeActionListener(al);
        }
        backButton.addActionListener(action);
    }

    /**
     * Returns the main panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Add a book row to the table (required by functionality class)
     */
    public void addBookToTable(Object[] rowData) {
        DefaultTableModel model = (DefaultTableModel) inventoryTable.getModel();
        model.addRow(rowData);
    }
    
    /**
     * Clear the table (required by functionality class)
     */
    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) inventoryTable.getModel();
        model.setRowCount(0);
    }
    
    /**
     * Show error message (required by functionality class)
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Simple testing method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Library Inventory");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new LibraryInventoryUI());
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}