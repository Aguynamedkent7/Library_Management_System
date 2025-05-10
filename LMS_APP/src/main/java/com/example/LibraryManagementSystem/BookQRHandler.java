package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specialized handler for processing book QR codes.
 * Extracts and processes book information from QR code content.
 */
public class BookQRHandler {
    private final ManageBooksFunction booksFunction;
    private final Pattern titlePattern = Pattern.compile("Title: (.+)");
    
    /**
     * Create a new book QR handler
     * 
     * @param booksFunction The book management function to process book returns
     */
    public BookQRHandler(ManageBooksFunction booksFunction) {
        this.booksFunction = booksFunction;
    }
    
    /**
     * Process QR code content to extract book information
     * 
     * @param qrContent The content of the scanned QR code
     * @return true if the content was processed as a book QR code, false otherwise
     */
    public boolean processQRContent(String qrContent) {
        // Check if this is a book QR code
        if (qrContent == null || !qrContent.contains("=== Book Information ===")) {
            return false;
        }
        
        try {
            // Extract the book title from the QR code
            Matcher matcher = titlePattern.matcher(qrContent);
            if (matcher.find()) {
                final String title = matcher.group(1);
                
                // Process the book return
                SwingUtilities.invokeLater(() -> {
                    // First try to find and select the book in the table
                    findAndSelectBook(title);
                    
                    // Then process the return
                    booksFunction.processBookReturn(title);
                });
                
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            // Show error in the view
            SwingUtilities.invokeLater(() -> 
                booksFunction.getView().showError("Error processing book QR code: " + e.getMessage()));
        }
        
        return false;
    }
    
    /**
     * Find a book in the table by title and select it
     * 
     * @param title The title of the book to find
     * @return true if found, false otherwise
     */
    private boolean findAndSelectBook(String title) {
        JTable bookTable = booksFunction.getView().getBookTable();
        int rowCount = bookTable.getRowCount();
        
        for (int i = 0; i < rowCount; i++) {
            String bookTitle = (String) bookTable.getValueAt(i, 0);
            if (title.equals(bookTitle)) {
                booksFunction.selectBookInTable(i);
                return true;
            }
        }
        
        return false;
    }
}