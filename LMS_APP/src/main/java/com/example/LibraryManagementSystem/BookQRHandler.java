package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specialized handler for processing book QR codes.
 * Extracts and processes book information from QR code content.
 */
public class BookQRHandler {
    private final ScanQrReturnBookFunction scanQrReturnBookFunction;

    public BookQRHandler(ScanQrReturnBookFunction scanQrReturnBookFunction) {
        this.scanQrReturnBookFunction = scanQrReturnBookFunction;
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
            // Extract the book copy ID from the QR code
            Pattern copyIDPattern = Pattern.compile("Book Copy ID: (\\d+)");
            Matcher matcher = copyIDPattern.matcher(qrContent);
            if (matcher.find()) {
                try {
                    final int bookCopyID = Integer.parseInt(matcher.group(1));
                    scanQrReturnBookFunction.returnBookByScan(bookCopyID);
                    JOptionPane.showMessageDialog(null,
                            "Book returned successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } catch (NumberFormatException | SQLException e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to return book: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error processing book QR Code: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

        }
        
        return false;
    }
}