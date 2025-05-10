package com.example.LibraryManagementSystem;

import com.github.sarxos.webcam.WebcamPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main UI class for QR code scanning.
 * Separates the UI concerns from the QR scanning logic.
 */
public class QRScannerView {
    // UI components
    private final JFrame window;
    private final JPanel webcamContainer;
    private final JLabel statusLabel;
    private final JTextArea resultTextArea;
    
    // Services
    private final QRCodeService qrService;
    private final QRContentWindow contentWindow;
    private final BookQRHandler bookHandler;
    
    // For integration with ManageBooks
    private ManageBooksFunction manageBooksFunction;
    
    /**
     * Create a new QR scanner view
     */
    public QRScannerView() {
        // Create UI components
        window = new JFrame("QR Code Scanner");
        window.setLayout(new BorderLayout());
        window.setSize(640, 480);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        webcamContainer = new JPanel(new BorderLayout());
        
        statusLabel = new JLabel("Initializing...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setRows(3);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Start Scanning");
        JButton stopButton = new JButton("Stop Scanning");
        JButton clearButton = new JButton("Clear Results");
        
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(clearButton);
        
        // Layout
        window.add(webcamContainer, BorderLayout.CENTER);
        window.add(statusLabel, BorderLayout.NORTH);
        window.add(scrollPane, BorderLayout.SOUTH);
        window.add(buttonPanel, BorderLayout.PAGE_END);
        
        // Initialize services
        contentWindow = new QRContentWindow();
        
        // Create QR service with event handlers
        qrService = new QRCodeService(
            this::handleQRDetected,
            this::updateStatus,
            this::handleError
        );
        
        // Create book handler
        bookHandler = new BookQRHandler(window, null);
        
        // Add action listeners
        startButton.addActionListener(e -> startScanning());
        stopButton.addActionListener(e -> stopScanning());
        clearButton.addActionListener(e -> resultTextArea.setText(""));
        
        // Window listeners
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                initializeWebcam();
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }
    
    /**
     * Initialize the webcam
     */
    private void initializeWebcam() {
        qrService.initializeWebcam(() -> {
            // Add webcam panel to container
            WebcamPanel panel = qrService.getWebcamPanel();
            webcamContainer.removeAll();
            webcamContainer.add(panel, BorderLayout.CENTER);
            webcamContainer.revalidate();
            webcamContainer.repaint();
        });
    }
    
    /**
     * Start scanning for QR codes
     */
    public void startScanning() {
        qrService.startScanning();
    }
    
    /**
     * Stop scanning for QR codes
     */
    public void stopScanning() {
        qrService.stopScanning();
    }
    
    /**
     * Show the scanner window
     */
    public void show() {
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        
        if (!qrService.isInitializing() && qrService.getWebcamPanel() == null) {
            initializeWebcam();
        }
    }
    
    /**
     * Handle detected QR code
     */
    private void handleQRDetected(String text) {
        // Add to results
        resultTextArea.append(text + "\n");
        resultTextArea.setCaretPosition(resultTextArea.getDocument().getLength());
        
        // Show in separate window
        contentWindow.display(text, window);
        
        // Process if it's a book QR code
        if (manageBooksFunction != null) {
            bookHandler.processQRContent(text);
        }
    }
    
    /**
     * Update status display
     */
    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
    
    /**
     * Handle errors
     */
    private void handleError(Exception e) {
        e.printStackTrace();
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(window, 
                "Error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE)
        );
    }
    
    /**
     * Clean up resources
     */
    private void cleanup() {
        qrService.dispose();
        contentWindow.close();
    }
    
    /**
     * Get the last scanned result
     */
    public String getLastResult() {
        String text = resultTextArea.getText();
        if (text.isEmpty()) {
            return null;
        }
        
        String[] lines = text.split("\n");
        return lines[lines.length - 1];
    }
    
    /**
     * Integrate with ManageBooks functionality
     */
    public void integrateWithManageBooks(ManageBooksFunction booksFunction) {
        this.manageBooksFunction = booksFunction;
    }
    
    /**
     * Clean up all resources and dispose window
     */
    public void dispose() {
        cleanup();
        window.dispose();
    }
    
    /**
     * Get the window
     */
    public JFrame getWindow() {
        return window;
    }
}