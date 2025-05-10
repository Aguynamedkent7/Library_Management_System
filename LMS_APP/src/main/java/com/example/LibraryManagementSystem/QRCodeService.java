package com.example.LibraryManagementSystem;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
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
     * Initialize webcam asynchronously
     * 
     * @param onComplete Callback when initialization completes
     */
    public void initializeWebcam(Runnable onComplete) {
        if (initializing) {
            return;
        }
        
        initializing = true;
        notifyStatus("Initializing camera...");
        
        new Thread(() -> {
            try {
                // Get default webcam
                webcam = Webcam.getDefault();
                
                // If no default, try to find any available webcam
                if (webcam == null) {
                    List<Webcam> webcams = Webcam.getWebcams();
                    if (!webcams.isEmpty()) {
                        webcam = webcams.get(0);
                    }
                }
                
                if (webcam == null) {
                    throw new RuntimeException("No webcam detected");
                }
                
                // Close if already open
                if (webcam.isOpen()) {
                    webcam.close();
                }
                
                // Set resolution
                webcam.setViewSize(WebcamResolution.QVGA.getSize());
                
                // Open non-blocking
                webcam.open(false);
                
                // Create webcam panel for UI
                webcamPanel = new WebcamPanel(webcam);
                webcamPanel.setFPSDisplayed(true);
                webcamPanel.setMirrored(false);
                webcamPanel.setFPSLimit(15);
                
                notifyStatus("Ready to scan");
                initializing = false;
                
                if (onComplete != null) {
                    onComplete.run();
                }
            } catch (Exception e) {
                initializing = false;
                notifyError(e);
                notifyStatus("Error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Start scanning for QR codes
     */
    public void startScanning() {
        if (scanning) return;
        
        // If webcam isn't ready, initialize it first
        if (webcam == null || !webcam.isOpen()) {
            if (!initializing) {
                initializeWebcam(() -> startScanning());
            }
            return;
        }
        
        notifyStatus("Scanning for QR codes...");
        scanning = true;
        
        // Create scan task
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::scanQRCode, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stop scanning
     */
    public void stopScanning() {
        if (!scanning) return;
        
        scanning = false;
        notifyStatus("Scanning stopped");
        
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
            BufferedImage image = webcam.getImage();
            if (image == null) return;
            
            BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(
                    new BufferedImageLuminanceSource(image)
                )
            );
            
            Result result = new MultiFormatReader().decode(bitmap);
            if (result != null) {
                // QR code found
                String text = result.getText();
                notifyQRDetected(text);
                
                // Pause to avoid multiple detections
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            // No QR code found in this frame - normal, continue
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
    
    // Notification methods
    private void notifyQRDetected(String text) {
        if (onQRCodeDetected != null) {
            onQRCodeDetected.accept(text);
        }
    }
    
    private void notifyStatus(String status) {
        if (onStatusChange != null) {
            onStatusChange.accept(status);
        }
    }
    
    private void notifyError(Exception e) {
        if (onError != null) {
            onError.accept(e);
        }
    }
}