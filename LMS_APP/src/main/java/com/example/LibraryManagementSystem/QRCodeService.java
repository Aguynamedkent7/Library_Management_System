package com.example.LibraryManagementSystem;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Core service for QR code scanning functionality.
 * Handles webcam access and QR code detection without UI dependencies.
 */
public class QRCodeService {
    // Add this static variable to track the default camera across all instances
    private static Webcam defaultWebcam = null;
    
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private ScheduledExecutorService executor;
    private boolean scanning = false;
    private boolean initializing = false;
    
    // Event handlers
    private Consumer<String> onQRCodeDetected;
    private Consumer<String> onStatusChange;
    private Consumer<Exception> onError;
    
    /**
     * Create a new QR code service with event handlers
     */
    public QRCodeService(Consumer<String> onQRCodeDetected, 
                         Consumer<String> onStatusChange,
                         Consumer<Exception> onError) {
        this.onQRCodeDetected = onQRCodeDetected;
        this.onStatusChange = onStatusChange;
        this.onError = onError;
    }
    
    /**
     * Get the current default webcam
     * @return The default webcam, or null if none set
     */
    public static Webcam getDefaultWebcam() {
        return defaultWebcam;
    }
    
    /**
     * Set the default webcam for all QRCodeService instances
     * @param webcam The webcam to set as default
     */
    public static void setDefaultWebcam(Webcam webcam) {
        defaultWebcam = webcam;
    }
    
    /**
     * Check if a default webcam is set
     * @return true if a default webcam is set
     */
    public static boolean hasDefaultWebcam() {
        return defaultWebcam != null;
    }
    
    /**
     * Get a list of all available webcams
     * 
     * @return List of available webcams
     */
    public List<Webcam> getAvailableWebcams() {
        try {
            return Webcam.getWebcams();
        } catch (Exception e) {
            notifyError(e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Initialize a specific webcam asynchronously
     * 
     * @param selectedWebcam The webcam to initialize
     * @param onComplete Callback when initialization completes
     */
    public void initializeWebcam(Webcam selectedWebcam, Runnable onComplete) {
        if (initializing) {
            return;
        }
        
        initializing = true;
        notifyStatus("Initializing camera...");
        
        new Thread(() -> {
            try {
                // Set webcam to the selected one
                webcam = selectedWebcam;
                
                if (webcam == null) {
                    throw new RuntimeException("No webcam selected");
                }
                
                // Close webcam if it's already open
                if (webcam.isOpen()) {
                    webcam.close();
                }
                
                // Set a smaller resolution for faster initialization
                webcam.setViewSize(WebcamResolution.QVGA.getSize()); // 320x240 instead of VGA
                
                // Open webcam with non-blocking mode
                webcam.open(false);
                
                // Create webcam panel with performance optimizations
                webcamPanel = new WebcamPanel(webcam);
                webcamPanel.setFPSDisplayed(true);
                webcamPanel.setMirrored(false);
                webcamPanel.setFPSLimit(15); // Lower FPS limit for better performance
                
                initializing = false;
                notifyStatus("Camera ready.");
                
                // Call completion callback
                if (onComplete != null) {
                    SwingUtilities.invokeLater(onComplete);
                }
            } catch (Exception e) {
                initializing = false;
                notifyError(e);
            }
        }).start();
    }
    
    /**
     * Backward compatibility method - initializes default webcam
     */
    public void initializeWebcam(Runnable onComplete) {
        List<Webcam> webcams = getAvailableWebcams();
        if (webcams.isEmpty()) {
            notifyError(new RuntimeException("No webcams detected"));
            return;
        }
        
        initializeWebcam(webcams.get(0), onComplete);
    }
    
    /**
     * Start scanning for QR codes
     */
    public void startScanning() {
        if (scanning) return;
        
        if (webcam == null || !webcam.isOpen()) {
            notifyStatus("Camera not initialized.");
            return;
        }
        
        notifyStatus("Scanning for QR codes...");
        scanning = true;
        
        // Create a scheduled task to scan for QR codes every 100ms
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::scanQRCode, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stop scanning
     */
    public void stopScanning() {
        if (!scanning) return;
        
        notifyStatus("Scanning stopped");
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
     * Scan for QR codes in current webcam frame
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
                // QR code found - notify listener
                final String text = result.getText();
                
                // Notify on the EDT
                SwingUtilities.invokeLater(() -> {
                    notifyStatus("QR Code Found!");
                    notifyQRDetected(text);
                });
                
                // Small pause after finding a QR code to avoid multiple detections
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            // No QR code found in this frame, that's normal, just continue
        }
    }
    
    /**
     * Get the webcam panel for display in UI
     */
    public WebcamPanel getWebcamPanel() {
        return webcamPanel;
    }
    
    /**
     * Check if currently scanning
     */
    public boolean isScanning() {
        return scanning;
    }
    
    /**
     * Check if webcam is being initialized
     */
    public boolean isInitializing() {
        return initializing;
    }
    
    /**
     * Get the executor service (may be needed for integration)
     */
    public ScheduledExecutorService getExecutor() {
        return executor;
    }
    
    /**
     * Clean up resources
     */
    public void dispose() {
        stopScanning();
        
        if (webcamPanel != null) {
            webcamPanel.stop();
        }
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
    
    // Set event handlers (added for flexibility)
    public void setOnQRCodeDetected(Consumer<String> handler) {
        this.onQRCodeDetected = handler;
    }
    
    public void setOnStatusChange(Consumer<String> handler) {
        this.onStatusChange = handler;
    }
    
    public void setOnError(Consumer<Exception> handler) {
        this.onError = handler;
    }
    
    // Notification methods
    private void notifyQRDetected(String text) {
        if (onQRCodeDetected != null) {
            onQRCodeDetected.accept(text);
        }
    }
    
    private void notifyStatus(String status) {
        if (onStatusChange != null) {
            SwingUtilities.invokeLater(() -> onStatusChange.accept(status));
        }
    }
    
    private void notifyError(Exception e) {
        if (onError != null) {
            SwingUtilities.invokeLater(() -> onError.accept(e));
        }
    }
}