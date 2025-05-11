package com.example.LibraryManagementSystem;

import api.mutate;
import api.query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import models.Book;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManageBooksFunction {
    private ManageBooksUI view;
    
    // Replace single ReadQR with modular components
    private QRScannerView qrScanner;
    private BookQRHandler bookQRHandler;
    private QRCodeService qrService;

    public ManageBooksFunction(ManageBooksUI view) {
        this.view = view;
    }

    public JList<String> queryGenresFromDB() {
        DefaultListModel<String> genresListModel = new DefaultListModel<>();
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            ArrayList<String> genres = query.QueryAllGenres(conn);

            if (genres != null) {
                for (String genre : genres) {
                    genresListModel.addElement(genre);
                }
            }
            conn.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return new JList<>(genresListModel);
    }

    public void loadBooksFromDatabase() {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            ArrayList<Book> books = query.QueryAllBooks(conn);

            view.clearTable();

            assert books != null;
            for (Book book : books) {
                Object[] rowData = {
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getPublisher(),
                        book.getPublished_Date()
                };
                view.addBookToTable(rowData);
            }

            conn.close();
        } catch (SQLException | NullPointerException e) {
            view.showError("Error loading books: " + e.getMessage());
        }
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


        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            mutate.AddBookToDatabase(conn, title, author, genre, publisher, datePublished);
            view.clearForm();
            view.showMessage("Book added successfully!");
            loadBooksFromDatabase();
            // Generate QR code for the newly added book
            generateQRCode();
        } catch (SQLException e) {
            view.showError("Error adding book: " + e.getMessage());
            System.out.println(e.getMessage());
            return;
        }
    }

    public void updateBook() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to update");
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
            try {
                Connection conn = DriverManager.getConnection(System.getenv("LMS_DB_URL"));
                mutate.DeleteBookFromDatabase(conn, view.getSelectedBookID(selectedRow));
                loadBooksFromDatabase();
                conn.close();
            } catch (SQLException e) {
                view.showError("Error deleting book: " + e.getMessage());
                System.out.println(e.getMessage());
                return;
            }
            view.showMessage("Book deleted successfully!");
        }
    }

    /**
     * Open QR code reader to scan and process books
     * This method replaces both the old scanQR and returnBook functions
     */
    public void returnBook() {
        try {
            if (qrScanner == null) {
                // Initialize components in the proper order
                initializeQRComponents();
            }
            
            // Show the scanner window and start scanning automatically
            qrScanner.show();
            qrScanner.startScanning();
        } catch (Exception e) {
            view.showError("Error initializing QR scanner: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize QR scanner components using the recommended approach
     */
    private void initializeQRComponents() {
        // 1. Create QR service with event handlers
        qrService = new QRCodeService(
            // These handlers will be replaced by QRScannerView
            text -> {}, 
            status -> {}, 
            error -> view.showError("QR Scanner error: " + error.getMessage())
        );
        
        // 2. Create book handler that references this class
        bookQRHandler = new BookQRHandler(this);
        
        // 3. Create scanner view with the service and handler
        qrScanner = new QRScannerView(qrService, bookQRHandler);
    }

    /**
     * Select a book in the table by index
     * This method is called from the BookQRHandler when a book QR is scanned
     */
    public void selectBookInTable(int rowIndex) {
        if (rowIndex >= 0) {
            view.getBookTable().setRowSelectionInterval(rowIndex, rowIndex);
            view.getBookTable().scrollRectToVisible(
                view.getBookTable().getCellRect(rowIndex, 0, true));
        }
    }

    public void generateQRCode() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to generate QR code");
            return;
        }

        Object[] bookData = view.getBookAtRow(selectedRow);
        String qrContent = formatQRContent(bookData[1].toString(),
                bookData[2].toString(), bookData[3].toString(),
                bookData[4].toString(), bookData[5].toString());

        try {
            BufferedImage qrImage = generateQRCodeImage(qrContent);
            view.displayQRCode(new ImageIcon(qrImage));
        } catch (WriterException e) {
            view.showError("Failed to generate QR code: " + e.getMessage());
        }
    }

    /**
     * Processes a book after its QR code has been scanned
     * @param bookTitle The title of the book being processed
     */
    public void processBookReturn(String bookTitle) {
        // In a real application, you would update a database to mark the book as returned
        // For this example, we'll just show a confirmation message
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                view.getFrame(),
                "Book \"" + bookTitle + "\" has been successfully returned.",
                "Book Return Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private String formatQRContent(String title, String author, String genre, String publisher, String date) {
        StringBuilder content = new StringBuilder();
        content.append("=== Book Information ===\n\n");
        content.append("Title: ").append(title).append("\n");
        content.append("Author: ").append(author).append("\n");
        if (!genre.isEmpty()) content.append("Genre: ").append(genre).append("\n");
        if (!publisher.isEmpty()) content.append("Publisher: ").append(publisher).append("\n");
        if (!date.isEmpty()) content.append("Date Published: ").append(date).append("\n");
        return content.toString();
    }

    private BufferedImage generateQRCodeImage(String text) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                300,
                300
        );
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public void goBack() {
        // Clean up resources properly
        cleanupQRComponents();
        view.getFrame().dispose();
    }
    
    /**
     * Clean up QR scanner components
     */
    private void cleanupQRComponents() {
        if (qrScanner != null) {
            qrScanner.dispose();
            qrScanner = null;
        }
        
        if (qrService != null) {
            qrService.dispose();
            qrService = null;
        }
        
        // No need to dispose bookQRHandler as it has no resources
        bookQRHandler = null;
    }
    
    // Getter for the UI view (needed by BookQRHandler)
    public ManageBooksUI getView() {
        return view;
    }
}