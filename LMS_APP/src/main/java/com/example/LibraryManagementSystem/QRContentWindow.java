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
        window = new JFrame("QR Code Content");
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
        
        JScrollPane scrollPane = new JScrollPane(contentArea);
        window.add(scrollPane, BorderLayout.CENTER);
        
        // Copy button
        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(this::copyToClipboard);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(copyButton);
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
     * Copy content to clipboard
     */
    private void copyToClipboard(ActionEvent e) {
        contentArea.selectAll();
        contentArea.copy();
        contentArea.setCaretPosition(0);
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