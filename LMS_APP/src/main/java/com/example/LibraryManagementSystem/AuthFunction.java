package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AuthFunction {
    private JFrame parentFrame;
    
    public AuthFunction(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void loginFunction(String username, String password) {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);

            int user_id = api.auth.login(conn, username, password);

            if (user_id == -1) {
                JOptionPane.showMessageDialog(parentFrame, "Invalid username or password");
            } else {
                // Create a message dialog that will disappear after 3 seconds
                final JDialog successDialog = new JDialog(parentFrame, "Success", true);
                JLabel messageLabel = new JLabel("Login successful!");
                messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                messageLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                successDialog.add(messageLabel);
                successDialog.pack();
                successDialog.setLocationRelativeTo(parentFrame);

                // Create a timer to close the dialog after 3 seconds
                Timer timer = new Timer(3000, e -> {
                    successDialog.dispose();
                    
                    // Open the AdminDashboard and dispose the login page
                    SwingUtilities.invokeLater(() -> {
                        AdminDashboard dashboard = new AdminDashboard();
                        dashboard.setVisible(true);
                        parentFrame.dispose(); // Close the parent frame
                    });
                });
                timer.setRepeats(false); // Only fire once
                timer.start();

                // Show the dialog (this will block until disposed)
                successDialog.setVisible(true);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(parentFrame, "Error with login: " + e.getMessage());
        }
    }
}