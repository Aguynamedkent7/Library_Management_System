package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class ManageBooksUI {
    private JFrame frame;
    private JTable bookTable;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField genreField;
    private JTextField publisherField;
    private JTextField datePublishedField;
    private ManageBooksFunctions controller;

    public ManageBooksUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table panel
        String[] columnNames = {"Title", "Author", "Genre", "Publisher", "Date Published"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        bookTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bookTable);

        // Form panel for adding/editing books
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        formPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        formPanel.add(authorField);

        formPanel.add(new JLabel("Genre:"));
        genreField = new JTextField();
        formPanel.add(genreField);

        formPanel.add(new JLabel("Publisher:"));
        publisherField = new JTextField();
        formPanel.add(publisherField);

        formPanel.add(new JLabel("Date Published:"));
        datePublishedField = new JTextField();
        formPanel.add(datePublishedField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(e -> controller.addBook());

        JButton editButton = new JButton("Edit Book");
        editButton.addActionListener(e -> controller.editBook());

        JButton deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(e -> controller.deleteBook());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> controller.goBack());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        // Assemble components
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
    }

    public void setController(ManageBooksFunctions controller) {
        this.controller = controller;
    }

    public void show() {
        frame.setVisible(true);
    }

    public String getTitle() {
        return titleField.getText();
    }

    public String getAuthor() {
        return authorField.getText();
    }

    public String getGenre() {
        return genreField.getText();
    }

    public String getPublisher() {
        return publisherField.getText();
    }

    public String getDatePublished() {
        return datePublishedField.getText();
    }

    public void clearForm() {
        titleField.setText("");
        authorField.setText("");
        genreField.setText("");
        publisherField.setText("");
        datePublishedField.setText("");
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addBookToTable(String[] bookData) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.addRow(bookData);
    }

    public int getSelectedBookRow() {
        return bookTable.getSelectedRow();
    }

    public String[] getBookAtRow(int row) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        String[] bookData = new String[5];
        for (int i = 0; i < 5; i++) {
            bookData[i] = model.getValueAt(row, i).toString();
        }
        return bookData;
    }

    public void updateBookInTable(int row, String[] bookData) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        for (int i = 0; i < 5; i++) {
            model.setValueAt(bookData[i], row, i);
        }
    }

    public void removeBookFromTable(int row) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.removeRow(row);
    }

    public JFrame getFrame() {
        return frame;
    }
}