package com.example.LibraryManagementSystem;

import api.MutateBooks;
import models.BorrowedBook;
import api.Query;

import javax.swing.*;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class BorrowedBooksFunction {
    private BorrowedBooksUI view;

    public BorrowedBooksFunction(BorrowedBooksUI view) {
        this.view = view;
    }

    public void loadBorrowedBooks() {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            ArrayList<BorrowedBook> borrowedBooks = Query.QueryAllBookBorrowers(conn);

            view.clearTable();

            for (BorrowedBook book : borrowedBooks) {
                Object[] rowData = {
                        book.getReferenceID(),
                        book.getFirstName(),
                        book.getLastName(),
                        book.getBookCopyID(),
                        book.getBookTitle(),
                        book.getBookAuthor(),
                        book.getBorrowDate(),
                        book.getReturnDate()
                };
                view.addBookToTable(rowData);
            }

            conn.close();
        } catch (SQLException | NullPointerException e) {
            view.showError("Error loading borrowed books: " + e.getMessage());
        }
    }

    public void showReturnDialog() {
        // Main panel and layout setup
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel instruction = new JLabel("Select a row for book return.");
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(instruction);
        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JButton cancelButton = new JButton("Cancel");
        JButton returnButton = new JButton("Return Book");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.add(cancelButton);
        buttonPanel.add(returnButton);
        mainPanel.add(buttonPanel);

        // Dialog setup without parent frame (null), modal
        JDialog dialog = new JDialog((Frame) null, "Return Book", true);
        dialog.setContentPane(mainPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null); // Center on screen

        // Button actions
        cancelButton.addActionListener(e -> dialog.dispose());

        returnButton.addActionListener(e -> {
            dialog.dispose();
            int selectedRow = view.getSelectedBookRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select a book to return.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int bookCopyID = view.getSelectedRowBookCopyID(selectedRow);

            try {
                String url = System.getenv("LMS_DB_URL");
                Connection conn = DriverManager.getConnection(url);
                MutateBooks.ReturnBook(conn, bookCopyID);
                conn.close();
                JOptionPane.showMessageDialog(dialog, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBorrowedBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error returning book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Accessibility: Enter to return, Esc to cancel
        dialog.getRootPane().setDefaultButton(returnButton);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setVisible(true);
    }
}