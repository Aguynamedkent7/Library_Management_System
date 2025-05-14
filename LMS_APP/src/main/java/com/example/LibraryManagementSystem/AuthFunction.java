package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AuthFunction {
    private LoginPage loginPage;

    public AuthFunction(LoginPage loginPage) {
        this.loginPage = loginPage;
    }

    public void loginFunction(String username, String password) {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);

            int user_id = api.auth.login(conn, username, password);

            if (user_id == -1) {
                JOptionPane.showMessageDialog(loginPage, "Invalid username or password");
            } else {
                JOptionPane.showMessageDialog(loginPage, "Login successful!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(loginPage, "Error with login: " + e.getMessage());
        }
    }
}
