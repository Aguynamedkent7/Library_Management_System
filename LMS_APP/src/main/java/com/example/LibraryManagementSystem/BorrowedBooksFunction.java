package com.example.LibraryManagementSystem;

import models.BorrowedBook;
import api.Query;
import javax.swing.JOptionPane;

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
    
    public void returnBook(int bookCopyID) {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            api.MutateBooks.ReturnBook(conn, bookCopyID);
            JOptionPane.showMessageDialog(null, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBorrowedBooks(); // Refresh the table
            conn.close();
        } catch (SQLException e) {
            view.showError("Error returning book: " + e.getMessage());
        }
    }
}