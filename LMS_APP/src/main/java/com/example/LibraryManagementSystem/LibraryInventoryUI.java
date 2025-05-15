package com.example.LibraryManagementSystem;

import models.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibraryInventoryUI extends JPanel {
    private JTable inventoryTable;
    private JLabel headerLabel;
    private Font headerFont = new Font("Arial", Font.BOLD, 20);
    private JPanel mainPanel;
    private JScrollPane tableScrollPane;
    private Connection connection;
    private final String DB_URL = "jdbc:mysql://localhost:3306/library_management";
    private final String USER = "root";
    private final String PASS = "";

    public LibraryInventoryUI() {
        initializeUI();
        loadAllBooks();
    }

    private void initializeUI() {
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
        
        // Set up the table columns
        String[] columnNames = {"ID", "Title", "Author", "Genre", "Publisher", "Date Published", "Total Copies", "Available Copies"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        inventoryTable.setModel(model);
        
        // Add table to a scroll pane
        tableScrollPane = new JScrollPane(inventoryTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Control panel at the bottom with buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAllBooks());
        JButton backButton = new JButton("Back");
        
        controlPanel.add(refreshButton);
        controlPanel.add(backButton);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Add the main panel to this JPanel
        setLayout(new BorderLayout());
        add(mainPanel);
    }
    
    /**
     * Loads all books from the database into the table
     */
    public void loadAllBooks() {
        DefaultTableModel model = (DefaultTableModel) inventoryTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        try {
            List<Book> books = fetchAllBooksFromDatabase();
            for (Book book : books) {
                Object[] row = {
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getGenre(),
                    book.getPublisher(),
                    book.getPublished_Date(),
                    getTotalCopies(book.getId()),
                    book.getAvailableCopies()
                };
                model.addRow(row);
            }
            
            if (model.getRowCount() > 0) {
                inventoryTable.setRowSelectionInterval(0, 0); // Select the first row
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading books: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Fetches all books from the database
     */
    private List<Book> fetchAllBooksFromDatabase() throws SQLException {
        List<Book> books = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT * FROM books ORDER BY title";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");
                String publisher = rs.getString("publisher");
                String publishedDate = rs.getString("published_date");
                int availableCopies = rs.getInt("available_copies");
                
                Book book = new Book(id, title, author, genre, publisher, publishedDate, availableCopies);
                books.add(book);
            }
        } finally {
            // Close resources
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        
        return books;
    }
    
    /**
     * Gets total copies (both available and currently borrowed) for a book
     */
    private int getTotalCopies(int bookId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int totalCopies = 0;
        
        try {
            conn = getConnection();
            // This query assumes you have a 'borrowed_books' table tracking borrowed copies
            String query = "SELECT COUNT(*) as borrowed FROM borrowed_books WHERE book_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            int borrowedCopies = 0;
            if (rs.next()) {
                borrowedCopies = rs.getInt("borrowed");
            }
            
            // Get available copies
            rs.close();
            stmt.close();
            
            query = "SELECT available_copies FROM books WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int availableCopies = rs.getInt("available_copies");
                totalCopies = availableCopies + borrowedCopies;
            }
        } finally {
            // Close resources
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        
        return totalCopies;
    }
    
    /**
     * Gets a database connection
     */
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        }
        return connection;
    }
    
    /**
     * Updates the back button's action
     */
    public void setBackButtonAction(java.awt.event.ActionListener action) {
        JPanel controlPanel = (JPanel) mainPanel.getComponent(2); // The control panel is the third component
        for (Component component : controlPanel.getComponents()) {
            if (component instanceof JButton && ((JButton) component).getText().equals("Back")) {
                JButton backButton = (JButton) component;
                // Remove existing action listeners
                for (java.awt.event.ActionListener al : backButton.getActionListeners()) {
                    backButton.removeActionListener(al);
                }
                backButton.addActionListener(action);
                break;
            }
        }
    }
    
    /**
     * Returns the main panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Simple testing method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Library Inventory");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new LibraryInventoryUI());
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}