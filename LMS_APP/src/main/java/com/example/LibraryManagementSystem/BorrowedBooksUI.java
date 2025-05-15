package com.example.LibraryManagementSystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BorrowedBooksUI {
    private JTable bookTable;
    private JLabel headerLabel;
    private JPanel contentPanel;
    private BorrowedBooksFunction controller;

    public BorrowedBooksUI(JTable bookTable, JLabel headerLabel) {
        this.bookTable = bookTable;
        this.headerLabel = headerLabel;
        this.controller = new BorrowedBooksFunction(this);
        initializeUI();
    }

    private void initializeUI() {
        contentPanel = new JPanel(new BorderLayout());
        
        // Set up the table with the borrowed books columns
        String[] columnNames = {"Reference ID", "Borrower First Name", "Borrower Last Name", "Book Copy ID", 
                               "Book Title", "Book Author", "Borrow Date", "Return Date"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        bookTable.setModel(model);
        
        // Hide the Book Copy ID column
        bookTable.getColumnModel().removeColumn(bookTable.getColumnModel().getColumn(3));
        
        // Update header
        headerLabel.setText("All Borrowers");
        
        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(bookTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Load the borrowed books data
        controller.loadBorrowedBooks();
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }
    
    public JLabel getHeaderLabel() {
        return headerLabel;
    }

    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.setRowCount(0);
    }

    public void addBookToTable(Object[] rowData) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.addRow(rowData);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public JTable getBookTable() {
        return bookTable;
    }
}