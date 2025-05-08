package com.example.LibraryManagementSystem;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class AddBook_Function {
    private AddBookUI view;

    public AddBook_Function(AddBookUI view) {
        this.view = view;
    }

    public void generateQRCode() {
        String bookTitle = view.getBookTitle();
        String author = view.getAuthor();
        String genre = view.getGenre();
        String datePublished = view.getDatePublished();

        if (bookTitle.isEmpty() && author.isEmpty() && genre.isEmpty() && datePublished.isEmpty()) {
            view.showError("Please enter at least one field");
            return;
        }

        String qrContent = formatQRContent(bookTitle, author, genre, datePublished);

        try {
            BufferedImage qrImage = generateQRCodeImage(qrContent);
            view.displayQRCode(new ImageIcon(qrImage));
        } catch (WriterException e) {
            view.showError("Failed to generate QR code: " + e.getMessage());
        }
    }

    private String formatQRContent(String title, String author, String genre, String date) {
        StringBuilder content = new StringBuilder();
        content.append("=== Book Information ===\n\n");
        if (!title.isEmpty()) content.append("Title: ").append(title).append("\n");
        if (!author.isEmpty()) content.append("Author: ").append(author).append("\n");
        if (!genre.isEmpty()) content.append("Genre: ").append(genre).append("\n");
        if (!date.isEmpty()) content.append("Published: ").append(date).append("\n");
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
}
