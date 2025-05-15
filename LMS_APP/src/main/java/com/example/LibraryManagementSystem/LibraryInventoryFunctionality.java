package com.example.LibraryManagementSystem;

import api.Query;
import models.Book;
import models.BorrowedBook;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class LibraryInventoryFunctionality {
    private LibraryInventoryUI view;
    private Connection connection;

    public LibraryInventoryFunctionality(LibraryInventoryUI view) {
        this.view = view;
    }

    /**
     * Formats the QR content in a standardized format
     */
    public String formatQRContent(String title, String author, String genre, String publisher, String date) {
        return String.format("Title: %s\nAuthor: %s\nGenre: %s\nPublisher: %s\nDate Published: %s", 
                title, author, genre, publisher, date);
    }
    
    /**
     * Generates QR code image from text content
     */
    public BufferedImage generateQRCodeImage(String text) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                300,
                300
        );
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    
    /**
     * Saves the generated QR code to a file
     */
    public void saveQRCodeToFile(JLabel qrCodeLabel, Component parent) {
        if (qrCodeLabel.getIcon() == null) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save QR Code");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Files", "png"));
        
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                file = new File(filePath + ".png");
            }
            
            try {
                // Convert Icon to BufferedImage
                Image img = ((ImageIcon) qrCodeLabel.getIcon()).getImage();
                BufferedImage bi = new BufferedImage(
                    img.getWidth(null), 
                    img.getHeight(null), 
                    BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.createGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();
                
                // Save the image
                ImageIO.write(bi, "png", file);
                JOptionPane.showMessageDialog(parent, 
                    "QR code saved successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent, 
                    "Error saving QR code: " + e.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Placeholder for loadAllBooks method (to be implemented)
     */
    public void loadAllBooks() {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            ArrayList<Book> books = Query.BookInventory(conn);

            view.clearTable();

            assert books != null;
            for (Book book : books) {
                Object[] rowData = {
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getPublisher(),
                        book.getPublished_Date(),
                        book.getAvailableCopies()
                };
                view.addBookToTable(rowData);
            }

            conn.close();
        } catch (SQLException | NullPointerException e) {
            view.showError("Error loading books: " + e.getMessage());
        }
    }

    /**
     * Gets total copies (both available and currently borrowed) for a book
     */
    public int getTotalCopies(int bookId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int totalCopies = 0;
        
        try {
            conn = getConnection();
            // This query assumes you have a 'borrowed_books' table tracking borrowed copies
            String query = "SELECT COUNT(*) as borrowed FROM borrowed_books WHERE book_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            int borrowedCopies = 0;
            if (rs.next()) {
                borrowedCopies = rs.getInt("borrowed");
            }
            
            // Get available copies
            rs.close();
            stmt.close();
            
            query = "SELECT available_copies FROM books WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, bookId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int availableCopies = rs.getInt("available_copies");
                totalCopies = availableCopies + borrowedCopies;
            }
        } finally {
            // Close resources
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        
        return totalCopies;
    }
    
    /**
     * Loads all borrowed books from the database
     */
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
                        book.getBookTitle(),
                        book.getBookAuthor(),
                        book.getBorrowDate(),
                        book.getReturnDate()
                };
                view.addBookToTable(rowData);
            }

            conn.close();
        } catch (SQLException | NullPointerException e) {
            view.showError("Error loading books: " + e.getMessage());
        }
    }
    
    /**
     * Establishes a connection to the database
     */
    private Connection getConnection() throws SQLException {
        String url = System.getenv("LMS_DB_URL");
        return DriverManager.getConnection(url);
    }
}