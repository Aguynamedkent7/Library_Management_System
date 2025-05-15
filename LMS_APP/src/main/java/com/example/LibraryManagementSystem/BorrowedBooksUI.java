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

    public int getSelectedBookID(int selectedRow) {
        // Convert view index to model index in case table is sorted
        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        // Assuming ID is stored in the first column (index 0)
        Object idValue = bookTable.getModel().getValueAt(modelRow, 0);
        return Integer.parseInt(idValue.toString());

    }

    public int getSelectedRowBookCopyID(int selectedRow) {
        // Convert view index to model index in case table is sorted
        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        // Assuming ID is stored in the first column (index 0)
        Object idValue = bookTable.getModel().getValueAt(modelRow, 3);
        return Integer.parseInt(idValue.toString());
    }

    public int getSelectedBookAvailableCopies(int selectedRow) {
        // Convert view index to model index in case table is sorted
        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        // Assuming ID is stored in the first column (index 0)
        Object idValue = bookTable.getModel().getValueAt(modelRow, 6);
        return Integer.parseInt(idValue.toString());

    }

    public int getSelectedBookRow() {
        return bookTable.getSelectedRow();
    }

    public BorrowedBooksFunction getController() {
        return controller;
    }
}