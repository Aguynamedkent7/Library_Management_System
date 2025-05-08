package com.example.LibraryManagementSystem;
import javax.swing.SwingUtilities;

public class LibraryManagementSystemApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddBookUI view = new AddBookUI();
            AddBook_Function controller = new AddBook_Function(view);
            view.setController(controller);
            view.show();
        });
    }
}
