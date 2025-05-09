package com.example.LibraryManagementSystem;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class ManageBooksFunction {
    private ManageBooksUI view;
    private ReadQR qrReader; // Add this line to declare the qrReader variable

    public ManageBooksFunction(ManageBooksUI view) {
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

        // Generate QR code for the newly added book
        generateQRCode();
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
            view.removeBookFromTable(selectedRow);
            view.showMessage("Book deleted successfully!");
        }
    }

    /**
     * Open QR code reader to scan and process books
     * This method replaces both the old scanQR and returnBook functions
     */
    public void returnBook() {
        try {
            if (qrReader == null) {
                qrReader = new ReadQR();
                // Set up the QR reader to handle book processing
                setupQRListener();
            }
            
            qrReader.show();
            qrReader.startScanning();
        } catch (Exception e) {
            view.showError("Error initializing webcam: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set up the QR reader to handle book processing
     */
    private void setupQRListener() {
        if (qrReader != null) {
            qrReader.integrateWithManageBooks(this);
        }
    }

    public void generateQRCode() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to generate QR code");
            return;
        }

        String[] bookData = view.getBookAtRow(selectedRow);
        String qrContent = formatQRContent(bookData[0], bookData[1], bookData[2], bookData[3], bookData[4]);

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
        // Close QR reader if it's open before going back
        if (qrReader != null) {
            qrReader.stopScanning();
        }

        view.showMessage("Returning to main menu...");
    }
}