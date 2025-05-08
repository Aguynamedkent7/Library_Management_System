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
    private JLabel qrCodeLabel;
    private ManageBooksFunction controller;

    public ManageBooksUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table panel
        String[] columnNames = {"Title", "Author", "Genre", "Publisher", "Date Published"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        bookTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bookTable);

        // Form panel
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(e -> controller.addBook());

        JButton editButton = new JButton("Edit Book");
        editButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow >= 0) {
                String[] bookData = getBookAtRow(selectedRow);
                titleField.setText(bookData[0]);
                authorField.setText(bookData[1]);
                genreField.setText(bookData[2]);
                publisherField.setText(bookData[3]);
                datePublishedField.setText(bookData[4]);
            }
        });

        JButton updateButton = new JButton("Update Book");
        updateButton.addActionListener(e -> controller.updateBook());

        JButton deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(e -> controller.deleteBook());

        JButton qrButton = new JButton("Generate QR");
        qrButton.addActionListener(e -> controller.generateQRCode());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> controller.goBack());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(qrButton);
        buttonPanel.add(backButton);

        // QR Code panel
        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(JLabel.CENTER);
        qrCodeLabel.setPreferredSize(new Dimension(300, 300));
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBorder(BorderFactory.createTitledBorder("Book QR Code"));
        qrPanel.add(qrCodeLabel, BorderLayout.CENTER);

        // Right panel for QR code
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(qrPanel, BorderLayout.NORTH);
        rightPanel.setPreferredSize(new Dimension(350, 0));

        // Center panel for table and form
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Main layout
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        frame.add(mainPanel);
    }

    public void setController(ManageBooksFunction controller) {
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

    public void displayQRCode(ImageIcon qrImage) {
        qrCodeLabel.setIcon(qrImage);
        frame.revalidate();
        frame.repaint();
    }
}