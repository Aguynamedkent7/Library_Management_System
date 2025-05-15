package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ScanQrReturnBookFunction {
    // Replace single ReadQR with modular components
    private QRScannerView qrScanner;
    private BookQRHandler bookQRHandler;
    private QRCodeService qrService;

    /**
     * Initialize QR scanner components using the recommended approach
     */
    private void initializeQRComponents() {
        // 1. Create QR service with event handlers
        qrService = new QRCodeService(
                // These handlers will be replaced by QRScannerView
                text -> {},
                status -> {},
                error -> JOptionPane.showMessageDialog(null,
                        "Qr Scanner Error: " + error.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE)
        );

        // 2. Create book handler that references this class

        bookQRHandler = new BookQRHandler(this);

        // 3. Create scanner view with the service and handler
        qrScanner = new QRScannerView(qrService, bookQRHandler);
    }

    public void initializeQRScanner() {
        try {
            if (qrScanner == null) {
                // Initialize components in the proper order
                initializeQRComponents();
            }

            // Show the scanner window and start scanning automatically
            qrScanner.show();
            qrScanner.startScanning();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error initializing QR scanner: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cleanupQRComponents() {
        if (qrScanner != null) {
            qrScanner.dispose();
            qrScanner = null;
        }

        if (qrService != null) {
            qrService.dispose();
            qrService = null;
        }

        // No need to dispose bookQRHandler as it has no resources
        bookQRHandler = null;
    }

    public void returnBookByScan(int bookCopyID) throws SQLException {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            api.MutateBooks.ReturnBook(conn, bookCopyID);
            conn.close();
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }
}
