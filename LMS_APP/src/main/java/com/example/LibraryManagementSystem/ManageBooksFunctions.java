package com.example.LibraryManagementSystem;

import javax.swing.*;

public class ManageBooksFunctions {
    private ManageBooksUI view;

    public ManageBooksFunctions(ManageBooksUI view) {
        this.view = view;
    }

    public void addBook() {
        String title = view.getTitle();
        String author = view.getAuthor();
        String genre = view.getGenre();
        String publisher = view.getPublisher();
        String datePublished = view.getDatePublished();

        if (title.isEmpty() || author.isEmpty()) {
            view.showError("Title and Author are required fields");
            return;
        }

        String[] bookData = {title, author, genre, publisher, datePublished};
        view.addBookToTable(bookData);
        view.clearForm();
        view.showMessage("Book added successfully!");
    }

    public void editBook() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to edit");
            return;
        }

        String title = view.getTitle();
        String author = view.getAuthor();
        String genre = view.getGenre();
        String publisher = view.getPublisher();
        String datePublished = view.getDatePublished();

        if (title.isEmpty() || author.isEmpty()) {
            view.showError("Title and Author are required fields");
            return;
        }

        String[] bookData = {title, author, genre, publisher, datePublished};
        view.updateBookInTable(selectedRow, bookData);
        view.clearForm();
        view.showMessage("Book updated successfully!");
    }

    public void deleteBook() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view.getFrame(),
                "Are you sure you want to delete this book?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            view.removeBookFromTable(selectedRow);
            view.showMessage("Book deleted successfully!");
        }
    }

    public void goBack() {
        view.showMessage("Returning to main menu...");
        // Here you would typically close this window and show the main menu
        // view.getFrame().dispose();
        // new MainMenu().show();
    }
}