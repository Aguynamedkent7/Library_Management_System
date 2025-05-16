package com.example.LibraryManagementSystem;

import javax.swing.*;

public class LibraryManagementSystemApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);
        });
    }
}