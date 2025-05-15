package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Separate window for displaying QR code content.
 */
public class QRContentWindow {
    private JFrame window;
    private JTextArea contentArea;
    private BookQRHandler bookHandler;
    
    /**
     * Create a new QR content window
     * 
     * @param bookHandler The handler for book QR codes
     */
    public QRContentWindow(BookQRHandler bookHandler) {
        this.bookHandler = bookHandler;
    }
    
    /**
     * Display QR content in a window
     * 
     * @param content The QR code content to display
     * @param parentWindow The parent window
     */
    public void display(String content, Window parentWindow) {
        // If window exists, update it
        if (window != null && window.isDisplayable()) {
            contentArea.setText(content);
            window.setVisible(true);
            window.toFront();
            return;
        }
        
        // Create new window
        window = new JFrame("Book Content");
        window.setSize(400, 300);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        // Content area
        contentArea = new JTextArea(content);
        contentArea.setEditable(false);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        contentArea.setMargin(new Insets(10, 10, 10, 10));
        contentArea.setFocusable(false);
        
        JScrollPane scrollPane = new JScrollPane(contentArea);
        window.add(scrollPane, BorderLayout.CENTER);
        
        // Return button
        JButton returnButton = new JButton("Return");
        returnButton.addActionListener(this::returnBook);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(returnButton);
        window.add(buttonPanel, BorderLayout.SOUTH);
        
        // Position window
        if (parentWindow != null) {
            window.setLocationRelativeTo(parentWindow);
        } else {
            window.setLocationRelativeTo(null);
        }
        
        window.setVisible(true);
    }
    
    /**
     * Return the book when the Return button is clicked
     */
    private void returnBook(ActionEvent e) {
        if (bookHandler != null && contentArea != null) {
            String content = contentArea.getText();
            bookHandler.processQRContent(content);
        }
        close();
    }
    
    /**
     * Close the window
     */
    public void close() {
        if (window != null) {
            window.dispose();
            window = null;
        }
    }
}