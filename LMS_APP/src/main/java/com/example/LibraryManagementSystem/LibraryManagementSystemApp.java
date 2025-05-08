package com.example.LibraryManagementSystem;
import javax.swing.SwingUtilities;

public class  LibraryManagementSystemApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageBooksUI view = new ManageBooksUI();
            ManageBooksFunctions controller = new ManageBooksFunctions(view);
            view.setController(controller);
            view.show();
        });
    }
}
