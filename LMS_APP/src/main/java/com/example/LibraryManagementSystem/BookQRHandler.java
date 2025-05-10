
package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Handler for book-specific QR code processing.
 * This separates the book-specific logic from the general QR scanning.
 */
public class BookQRHandler {
    private final Window parentWindow;
    private final Consumer<String> onBookFound;
    
    public BookQRHandler(Window parentWindow, Consumer<String> onBookFound) {
        this.parentWindow = parentWindow;
        this.onBookFound = onBookFound;
    }
    
    /**
     * Process QR code content for book information
     * 
     * @param qrContent The QR code content
     * @return true if book information was found and processed
     */
    public boolean processQRContent(String qrContent) {
        if (qrContent == null || !qrContent.contains("Book Information")) {
            return false;
        }
        
        try {
            // Extract book title
            String bookTitle = extractBookTitle(qrContent);
            
            if (bookTitle != null && !bookTitle.isEmpty()) {
                // Notify listener
                if (onBookFound != null) {
                    onBookFound.accept(bookTitle);
                }
                
                // Show confirmation
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parentWindow,
                        "Found book: " + bookTitle,
                        "Book Found",
                        JOptionPane.INFORMATION_MESSAGE);
                });
                
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Extract book title from QR content
     */
    private String extractBookTitle(String qrContent) {
        String[] lines = qrContent.split("\n");
        for (String line : lines) {
            if (line.startsWith("Title: ")) {
                return line.substring(7).trim();
            }
        }
        return null;
    }
}