package com.example.LibraryManagementSystem;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class ReadQR {
    private final JFrame window;
    private final JLabel statusLabel;
    private final JTextArea resultTextArea;
    private final Webcam webcam;
    private ScheduledExecutorService executor;
    private boolean scanning = false;

    public ReadQR() {
        // Set up the main window
        window = new JFrame("QR Code Scanner");
        window.setLayout(new BorderLayout());
        window.setSize(640, 480);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Initialize the webcam with medium resolution
        webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new RuntimeException("No webcam detected");
        }
        
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        
        // Create webcam panel
        WebcamPanel webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setMirrored(false);
        
        // Create status label and result text area
        statusLabel = new JLabel("Ready to scan", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setRows(3);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        
        // Create control panel with buttons
        JPanel controlPanel = new JPanel();
        JButton startButton = new JButton("Start Scanning");
        JButton stopButton = new JButton("Stop Scanning");
        JButton clearButton = new JButton("Clear Results");
        
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(clearButton);
        
        // Add components to the window
        window.add(webcamPanel, BorderLayout.CENTER);
        window.add(statusLabel, BorderLayout.NORTH);
        window.add(scrollPane, BorderLayout.SOUTH);
        window.add(controlPanel, BorderLayout.PAGE_END);
        
        // Add event listeners
        startButton.addActionListener(e -> startScanning());
        stopButton.addActionListener(e -> stopScanning());
        clearButton.addActionListener(e -> resultTextArea.setText(""));
        
        // Clean up resources when the window is closed
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopScanning();
                if (webcam.isOpen()) {
                    webcam.close();
                }
            }
        });
    }
    
    /**
     * Start the QR code scanning process
     */
    public void startScanning() {
        if (scanning) return;
        
        // Open the webcam if it's not already open
        if (!webcam.isOpen()) {
            webcam.open();
        }
        
        statusLabel.setText("Scanning for QR codes...");
        scanning = true;
        
        // Create a scheduled task to scan for QR codes every 100ms
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::scanQRCode, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stop the QR code scanning process
     */
    public void stopScanning() {
        if (!scanning) return;
        
        statusLabel.setText("Scanning stopped");
        scanning = false;
        
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Scan for QR codes in the current webcam frame
     */
    private void scanQRCode() {
        try {
            // Capture image from webcam
            BufferedImage image = webcam.getImage();
            if (image == null) return;
            
            // Convert image to binary bitmap for ZXing
            BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(
                    new BufferedImageLuminanceSource(image)
                )
            );
            
            // Try to decode QR code
            Result result = new MultiFormatReader().decode(bitmap);
            if (result != null) {
                // QR code found - update UI with the result
                String text = result.getText();
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("QR Code Found!");
                    resultTextArea.append(text + "\n");
                    resultTextArea.setCaretPosition(resultTextArea.getDocument().getLength());
                });
                
                // Small pause after finding a QR code to avoid multiple detections
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            // No QR code found in this frame, that's normal, just continue
        }
    }
    
    /**
     * Show the QR code scanner window
     */
    public void show() {
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
    
    /**
     * Get the last scanned QR code result
     * 
     * @return The text of the last scanned QR code, or null if none
     */
    public String getLastResult() {
        String text = resultTextArea.getText();
        if (text.isEmpty()) {
            return null;
        }
        
        // Return the last line from the result text area
        String[] lines = text.split("\n");
        return lines[lines.length - 1];
    }
    
    // Method to integrate the QR reader with the ManageBooksFunction
    public void integrateWithManageBooks(ManageBooksFunction booksFunction) {
        // Listen for QR scan results and use them to look up books
        executor.scheduleAtFixedRate(() -> {
            String lastResult = getLastResult();
            if (lastResult != null && lastResult.contains("Book Information")) {
                // Process the QR code data to extract book information
                try {
                    // Example of extracting title from QR data
                    String[] lines = lastResult.split("\n");
                    for (String line : lines) {
                        if (line.startsWith("Title: ")) {
                            final String title = line.substring(7);
                            SwingUtilities.invokeLater(() -> {
                                // Here you could implement a search functionality
                                JOptionPane.showMessageDialog(window, 
                                    "Found book: " + title,
                                    "Book Found", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            });
                            break;
                        }
                    }
                } catch (Exception ex) {
                    // Handle parsing errors
                    ex.printStackTrace();
                }
            }
        }, 1, 2, TimeUnit.SECONDS);
    }
    
    public static void main(String[] args) {
        // This main method allows testing the QR code reader as a standalone application
        SwingUtilities.invokeLater(() -> {
            try {
                ReadQR qrReader = new ReadQR();
                qrReader.show();
                qrReader.startScanning();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Error initializing webcam: " + e.getMessage(),
                    "Webcam Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}