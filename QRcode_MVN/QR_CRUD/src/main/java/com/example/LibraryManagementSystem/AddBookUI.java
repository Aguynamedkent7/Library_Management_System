package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;

public class AddBookUI {
    private JFrame frame;
    private JTextField bookTitleField;
    private JTextField authorField;
    private JTextField genreField;
    private JTextField datePublishedField;
    private JLabel qrCodeLabel;
    private AddBook_Function controller;

    public AddBookUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Book QR Code Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.add(new JLabel("Book Title:"));
        bookTitleField = new JTextField();
        formPanel.add(bookTitleField);

        formPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        formPanel.add(authorField);

        formPanel.add(new JLabel("Genre:"));
        genreField = new JTextField();
        formPanel.add(genreField);

        formPanel.add(new JLabel("Date Published:"));
        datePublishedField = new JTextField();
        formPanel.add(datePublishedField);

        // Button
        JButton generateButton = new JButton("Add Book to Library");
        generateButton.addActionListener(e -> controller.generateQRCode());

        // QR Code display
        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(JLabel.CENTER);
        qrCodeLabel.setPreferredSize(new Dimension(300, 300));

        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBorder(BorderFactory.createTitledBorder("Generated QR Code"));
        qrPanel.add(qrCodeLabel, BorderLayout.CENTER);

        // Assemble components
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(generateButton, BorderLayout.CENTER);
        mainPanel.add(qrPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
    }

    public void setController(AddBook_Function controller) {
        this.controller = controller;
    }

    public void show() {
        frame.setVisible(true);
    }

    public String getBookTitle() {
        return bookTitleField.getText();
    }

    public String getAuthor() {
        return authorField.getText();
    }

    public String getGenre() {
        return genreField.getText();
    }

    public String getDatePublished() {
        return datePublishedField.getText();
    }

    public void displayQRCode(ImageIcon qrImage) {
        qrCodeLabel.setIcon(qrImage);
        frame.pack();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}