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
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.util.List;
import com.github.sarxos.webcam.WebcamException;

public class ReadQR {
    private final JFrame window;
    private final JLabel statusLabel;
    private final JTextArea resultTextArea;
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private final JPanel webcamContainer;
    private ScheduledExecutorService executor;
    private boolean scanning = false;

    public ReadQR() {
        // Set up the main window
        window = new JFrame("QR Code Scanner");
        window.setLayout(new BorderLayout());
        window.setSize(640, 480);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create a container for the webcam panel that we can refresh later
        webcamContainer = new JPanel(new BorderLayout());
        
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
        window.add(webcamContainer, BorderLayout.CENTER);
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
                cleanupResources();
            }
            
            @Override
            public void windowOpened(WindowEvent e) {
                // Initialize the webcam when the window is opened
                initializeWebcam();
            }
        });
    }

    /**
     * Initialize the webcam and its panel
     */
private void initializeWebcam() {
    try {
        // Try to get the default webcam
        webcam = Webcam.getDefault();
        
        // If no default webcam, try to get any available webcam
        if (webcam == null) {
            List<Webcam> webcams = Webcam.getWebcams();
            if (!webcams.isEmpty()) {
                webcam = webcams.get(0);
            }
        }
        
        if (webcam == null) {
            throw new RuntimeException("No webcam detected");
        }
        
        // Close webcam if it's already open
        if (webcam.isOpen()) {
            webcam.close();
        }
        
        // Set a standard resolution (VGA)
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        
        // Open the webcam
        webcam.open();
        
        // Create webcam panel with performance optimizations
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setMirrored(false);
        webcamPanel.setFPSLimit(20); // Limit FPS to reduce CPU usage
        
        // Add the webcam panel to the container
        webcamContainer.removeAll();
        webcamContainer.add(webcamPanel, BorderLayout.CENTER);
        webcamContainer.revalidate();
        webcamContainer.repaint();
        
        // Prepare for scanning
        if (!scanning) {
            statusLabel.setText("Ready to scan. Click 'Start Scanning' to begin.");
        }
    } catch (Exception e) {
        statusLabel.setText("Error initializing webcam: " + e.getMessage());
        e.printStackTrace();
    }
}

    /**
     * Clean up all resources
     */
    private void cleanupResources() {
        stopScanning();
        
        if (webcamPanel != null) {
            webcamPanel.stop();
        }
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    /**
     * Show the QR code scanner window
     */
    public void show() {
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        
        // Initialize if not already done
        if (webcam == null) {
            initializeWebcam();
        }
    }

    /**
     * Start the QR code scanning process
     */
    public void startScanning() {
        if (scanning) return;
        
        // Check if webcam is valid and open it if needed
        if (webcam == null || !webcam.isOpen()) {
            initializeWebcam();
            
            if (webcam == null) {
                statusLabel.setText("Cannot start scanning: No webcam detected");
                return;
            }
            
            if (!webcam.isOpen()) {
                try {
                    webcam.open(true); // Open the webcam immediately
                } catch (Exception e) {
                    statusLabel.setText("Failed to open webcam: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        }
        
        statusLabel.setText("Scanning for QR codes...");
        scanning = true;
        
        // Create a scheduled task to scan for QR codes every 100ms
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::scanQRCode, 0, 100, TimeUnit.MILLISECONDS);
        
        // If this is part of ManageBooks flow, set up book processing
        for (WindowListener listener : window.getWindowListeners()) {
            if (listener instanceof WindowAdapter) {
                WindowEvent event = new WindowEvent(window, WindowEvent.WINDOW_OPENED);
                listener.windowOpened(event);
            }
        }
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
        // Store the books function reference instead of immediately scheduling
        // The actual scheduling will happen when startScanning() is called
        final ManageBooksFunction booksFunctionRef = booksFunction;
        
        // Add a new method to the webcam window adapter to process books when a QR is found
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // After window is opened, this will make sure executor is initialized in startScanning first
                SwingUtilities.invokeLater(() -> {
                    if (scanning && executor != null) {
                        setupBookProcessing(booksFunctionRef);
                    }
                });
            }
        });
    }

    // New helper method to set up book processing when the executor is ready
    private void setupBookProcessing(ManageBooksFunction booksFunction) {
        if (executor == null) return;
        
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
        }, 1L, 2L, TimeUnit.SECONDS);
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

/**
 * Properly dispose of all resources
 */
public void dispose() {
    cleanupResources();
    window.dispose();
}
}