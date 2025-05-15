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

    public void generateQRCode(int selectedRow) {
        if (selectedRow == -1) {
            view.showError("Please select a book to generate QR code");
            return;
        }

        Object[] bookData = view.getBookAtRow(selectedRow);
        String qrContent = formatQRContent(bookData[0].toString(),
                bookData[1].toString(), bookData[2].toString(),
                bookData[3].toString(), bookData[4].toString(),
                bookData[5].toString());

        try {
            BufferedImage qrImage = generateQRCodeImage(qrContent);
            view.displayQRCode(new ImageIcon(qrImage));
        } catch (WriterException e) {
            view.showError("Failed to generate QR code: " + e.getMessage());
        }
    }


    /**
     * Formats the QR content in a standardized format
     */
    private String formatQRContent(String book_copy_id, String title, String author, String genre, String publisher, String date) {
        StringBuilder content = new StringBuilder();
        content.append("=== Book Information ===\n\n");
        content.append("Book Copy ID: ").append(book_copy_id).append("\n");
        content.append("Title: ").append(title).append("\n");
        content.append("Author: ").append(author).append("\n");
        if (!genre.isEmpty()) content.append("Genre: ").append(genre).append("\n");
        if (!publisher.isEmpty()) content.append("Publisher: ").append(publisher).append("\n");
        if (!date.isEmpty()) content.append("Date Published: ").append(date).append("\n");
        return content.toString();
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
}