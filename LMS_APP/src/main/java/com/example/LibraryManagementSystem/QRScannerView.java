package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import java.util.List;

/**
 * UI component for QR code scanning.
 * Handles the display of webcam feed and scanning controls.
 */
public class QRScannerView {
    private final JFrame window;
    private final JLabel statusLabel;
    private final JTextArea resultTextArea;
    private final JPanel webcamContainer;
    private final QRCodeService qrService;
    private final BookQRHandler bookHandler;
    private final QRContentWindow contentWindow;
    private JComboBox<WebcamItem> cameraSelector;
    private JButton setDefaultButton;
    
    /**
     * Create a new QR scanner view
     * 
     * @param qrService The QR code service for handling scanning
     * @param bookHandler The handler for book QR codes
     */
    public QRScannerView(QRCodeService qrService, BookQRHandler bookHandler) {
        this.qrService = qrService;
        this.bookHandler = bookHandler;
        this.contentWindow = new QRContentWindow(bookHandler);
        
        // Set up the main window
        window = new JFrame("QR Code Scanner");
        window.setLayout(new BorderLayout());
        window.setSize(640, 480);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create a container for the webcam panel
        webcamContainer = new JPanel(new BorderLayout());
        
        // Create status label and result text area
        statusLabel = new JLabel("Ready to scan", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setRows(3);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        
        // Create camera selection panel
        JPanel cameraPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel cameraLabel = new JLabel("Select Camera: ");
        cameraSelector = new JComboBox<>();
        JButton refreshButton = new JButton("Refresh");
        setDefaultButton = new JButton("Set as Default");
        
        cameraPanel.add(cameraLabel);
        cameraPanel.add(cameraSelector);
        cameraPanel.add(refreshButton);
        cameraPanel.add(setDefaultButton);
        
        // Create control panel with buttons
        JPanel controlPanel = new JPanel();
        JButton startButton = new JButton("Start Scanning");
        JButton stopButton = new JButton("Stop Scanning");
        JButton clearButton = new JButton("Clear Results");
        
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(clearButton);
        
        // Combine camera selection and control panels
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(cameraPanel, BorderLayout.NORTH);
        bottomPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Add components to the window
        window.add(webcamContainer, BorderLayout.CENTER);
        window.add(statusLabel, BorderLayout.NORTH);
        window.add(scrollPane, BorderLayout.SOUTH);
        window.add(bottomPanel, BorderLayout.PAGE_END);
        
        // Add event listeners
        refreshButton.addActionListener(e -> populateCameraSelector());
        setDefaultButton.addActionListener(e -> setDefaultCamera());
        cameraSelector.addActionListener(e -> onCameraSelected());
        startButton.addActionListener(e -> startScanning());
        stopButton.addActionListener(e -> stopScanning());
        clearButton.addActionListener(e -> resultTextArea.setText(""));

        // Set up QR service event handlers
        setupQRServiceHandlers();
        
        // Clean up resources when the window is closed
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
            
            @Override
            public void windowOpened(WindowEvent e) {
                // Populate camera selector when window opens
                populateCameraSelector();
                
                // If there's a default webcam, select it
                selectDefaultWebcamIfAvailable();
            }
        });
    }
    
    /**
     * Set the currently selected camera as the default for the application
     */
    private void setDefaultCamera() {
        WebcamItem selectedItem = (WebcamItem) cameraSelector.getSelectedItem();
        if (selectedItem == null) {
            statusLabel.setText("Please select a camera first");
            return;
        }
        
        // Set the webcam as default in the service
        QRCodeService.setDefaultWebcam(selectedItem.getWebcam());
        statusLabel.setText("Camera set as default: " + selectedItem.getWebcam().getName());
        
        // Update button text
        setDefaultButton.setText("Default Camera");
        
        // Visual feedback for successful operation
        setDefaultButton.setEnabled(false);
        Timer enableTimer = new Timer(1500, e -> {
            setDefaultButton.setEnabled(true);
            // Update button text based on whether this camera is still the default
            boolean isDefault = QRCodeService.hasDefaultWebcam() && 
                            selectedItem.getWebcam().equals(QRCodeService.getDefaultWebcam());
            setDefaultButton.setText(isDefault ? "Default Camera" : "Set as Default");
        });
        enableTimer.setRepeats(false);
        enableTimer.start();
    }
    
    /**
     * Select the default webcam in the dropdown if available
     */
    private void selectDefaultWebcamIfAvailable() {
        if (!QRCodeService.hasDefaultWebcam()) {
            return;
        }
        
        Webcam defaultWebcam = QRCodeService.getDefaultWebcam();
        
        // Find and select the default webcam in the dropdown
        for (int i = 0; i < cameraSelector.getItemCount(); i++) {
            WebcamItem item = cameraSelector.getItemAt(i);
            if (item.getWebcam().equals(defaultWebcam)) {
                cameraSelector.setSelectedIndex(i);
                // Initialize this webcam
                onCameraSelected();
                break;
            }
        }
    }
    
    /**
     * Populate the camera selector dropdown with available webcams
     */
    private void populateCameraSelector() {
        // Store the currently selected item
        WebcamItem selectedItem = (WebcamItem) cameraSelector.getSelectedItem();
        
        // Stop scanning if active
        if (qrService.isScanning()) {
            qrService.stopScanning();
        }
        
        // Get available webcams
        List<Webcam> webcams = qrService.getAvailableWebcams();
        
        // Clear current items
        cameraSelector.removeAllItems();
        
        // Add webcams to dropdown
        if (webcams.isEmpty()) {
            statusLabel.setText("No cameras detected");
        } else {
            for (Webcam webcam : webcams) {
                cameraSelector.addItem(new WebcamItem(webcam));
            }
            
            // Try to reselect the previously selected item
            if (selectedItem != null) {
                for (int i = 0; i < cameraSelector.getItemCount(); i++) {
                    WebcamItem item = cameraSelector.getItemAt(i);
                    if (item.getWebcam().equals(selectedItem.getWebcam())) {
                        cameraSelector.setSelectedIndex(i);
                        break;
                    }
                }
            } 
            // If no previously selected item or not found, try to select default
            else if (QRCodeService.hasDefaultWebcam()) {
                selectDefaultWebcamIfAvailable();
            }
            
            statusLabel.setText("Select a camera and click 'Start Scanning'");
        }
    }
    
    /**
     * Handle camera selection change
     */
    private void onCameraSelected() {
        if (cameraSelector.getItemCount() == 0) return;
        
        // Stop scanning if active
        if (qrService.isScanning()) {
            qrService.stopScanning();
        }
        
        // Get selected webcam
        WebcamItem selectedItem = (WebcamItem) cameraSelector.getSelectedItem();
        if (selectedItem == null) return;
        
        // Update button state based on whether this camera is the default
        boolean isDefault = QRCodeService.hasDefaultWebcam() && 
                        selectedItem.getWebcam().equals(QRCodeService.getDefaultWebcam());
        setDefaultButton.setText(isDefault ? "Default Camera" : "Set as Default");
        
        // Initialize the selected webcam
        initializeWebcam(selectedItem.getWebcam());
    }
    
    /**
     * Set up handlers for QR service events
     */
    private void setupQRServiceHandlers() {
        qrService.setOnQRCodeDetected(this::handleQRDetected);
        qrService.setOnStatusChange(status -> statusLabel.setText(status));
        qrService.setOnError(e -> {
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        });
    }
    
    /**
     * Initialize the webcam
     */
    private void initializeWebcam(Webcam webcam) {
        // Show loading indicator
        JProgressBar loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        webcamContainer.removeAll();
        webcamContainer.add(loadingBar, BorderLayout.CENTER);
        webcamContainer.revalidate();
        webcamContainer.repaint();
        
        // Initialize webcam using the service
        qrService.initializeWebcam(webcam, () -> {
            // Update UI in EDT
            SwingUtilities.invokeLater(() -> {
                WebcamPanel webcamPanel = qrService.getWebcamPanel();
                if (webcamPanel != null) {
                    webcamContainer.removeAll();
                    webcamContainer.add(webcamPanel, BorderLayout.CENTER);
                    webcamContainer.revalidate();
                    webcamContainer.repaint();
                    
                    if (!qrService.isScanning()) {
                        statusLabel.setText("Ready to scan. Click 'Start Scanning' to begin.");
                    }
                } else {
                    statusLabel.setText("Failed to initialize webcam.");
                }
            });
        });
    }
    
    /**
     * Show the QR scanner window
     */
    public void show() {
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
    
    /**
     * Start scanning for QR codes
     */
    public void startScanning() {
        if (qrService.isScanning()) return;
        
        // If webcam is initializing, notify user to wait
        if (qrService.isInitializing()) {
            statusLabel.setText("Please wait, camera is still initializing...");
            return;
        }
        
        // If no webcam selected, prompt user
        if (cameraSelector.getSelectedItem() == null) {
            statusLabel.setText("Please select a camera first");
            return;
        }
        
        // Start scanning using the service
        qrService.startScanning();
    }
    
    /**
     * Stop scanning for QR codes
     */
    public void stopScanning() {
        if (!qrService.isScanning()) return;
        
        qrService.stopScanning();
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

    }
    
    /**
     * Dispose of resources
     */
    public void dispose() {
        stopScanning();
        
        if (contentWindow != null) {
            contentWindow.close();
        }
        
        window.dispose();
    }
    
    /**
     * Helper class to represent a webcam in the combobox
     */
    private static class WebcamItem {
        private final Webcam webcam;
        
        public WebcamItem(Webcam webcam) {
            this.webcam = webcam;
        }
        
        public Webcam getWebcam() {
            return webcam;
        }
        
        @Override
        public String toString() {
            return webcam.getName();
        }
    }
}