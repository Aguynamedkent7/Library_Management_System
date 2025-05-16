package com.example.LibraryManagementSystem;

import api.MutateBooks;
import api.Query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import models.Book;

import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ManageBooksFunction {
    private ManageBooksUI view;



    public ManageBooksFunction(ManageBooksUI view) {
        this.view = view;
    }

    public JList<String> queryGenresFromDB() {
        DefaultListModel<String> genresListModel = new DefaultListModel<>();
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            ArrayList<String> genres = Query.QueryAllGenres(conn);

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

    public void loadAvailableBooks() {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            ArrayList<Book> books = Query.QueryAllAvailableBooks(conn);

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
            MutateBooks.AddBookToDatabase(conn, title, author, genre, publisher, datePublished);
            view.clearForm();
            view.showMessage("Book added successfully!");
            loadAvailableBooks();
        } catch (SQLException e) {
            view.showError("Error adding book: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    public void updateBook() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to update");
            return;
        }

        int id = view.getSelectedBookID(selectedRow);
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
            MutateBooks.UpdateBookInDatabase(conn, id, title, author, genre, publisher, datePublished);
            view.clearForm();
            view.showMessage("Book updated successfully!");
            loadAvailableBooks();
        } catch (SQLException e) {
            view.showError("Error updating book: " + e.getMessage());
            System.out.println(e.getMessage());
        }
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
                MutateBooks.DeleteBookFromDatabase(conn, view.getSelectedBookID(selectedRow));
                loadAvailableBooks();
                conn.close();
            } catch (SQLException e) {
                view.showError("Error deleting book: " + e.getMessage());
                System.out.println(e.getMessage());
                return;
            }
            view.showMessage("Book deleted successfully!");
        }
    }

    private String formatQRContent(String borrowerName, String book_copy_id, String title, String author, String genre, String publisher, String date) {
            StringBuilder content = new StringBuilder();
            content.append("=== Book Information ===\n\n");
            content.append("Book Copy ID: ").append(book_copy_id).append("\n");
            content.append("Title: ").append(title).append("\n");
            content.append("Author: ").append(author).append("\n");
            if (!genre.isEmpty()) content.append("Genre: ").append(genre).append("\n");
            if (!publisher.isEmpty()) content.append("Publisher: ").append(publisher).append("\n");
            if (!date.isEmpty()) content.append("Date Published: ").append(date).append("\n");
            if (!borrowerName.isEmpty()) content.append("Borrower Name: ").append(borrowerName).append("\n");
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

    public void borrowBook() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to borrow");
            return;
        }
        int bookId = view.getSelectedBookID(selectedRow);

        JTextField fnameField = new JTextField(10);
        JTextField lnameField = new JTextField(10);
        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("Ok");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel label = new JLabel("Select a book, then enter borrower information");
        labelPanel.add(label);
        panel.add(labelPanel);

        // First name panel
        JPanel firstNamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        firstNamePanel.add(new JLabel("First name: "));
        firstNamePanel.add(fnameField);
        panel.add(firstNamePanel);

        // Last name panel
        JPanel lastNamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lastNamePanel.add(new JLabel("Last name: "));
        lastNamePanel.add(lnameField);
        panel.add(lastNamePanel);

        panel.add(buttonPanel);


        JDialog dialog = new JDialog(view.getFrame(), "Borrow Book", true);
        dialog.setContentPane(panel);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(view.getFrame());

        // return 3 weeks from borrow date
        String returnDate = Date.valueOf(LocalDate.now().plusWeeks(3)).toString();

        // Cancel button action
        cancelButton.addActionListener(e -> {
            dialog.dispose();
        });

        // Add button action
        okButton.addActionListener(e -> {
            if (fnameField.getText().trim().isEmpty() ||
                    lnameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Input fields cannot be empty.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            dialog.dispose();
            // Process the valid input
            try {
                String firstName = fnameField.getText().trim();
                String lastName = lnameField.getText().trim();
                String url = System.getenv("LMS_DB_URL");
                Connection conn = DriverManager.getConnection(url);
                int copy_id = MutateBooks.BorrowBook(conn, firstName, lastName, bookId, returnDate);

                String fullName = firstName + " " + lastName;
                // title, author, genres
                ArrayList<String> otherBookDetails = Query.QueryBookDetailsByCopyID(conn, copy_id);

                // String borrowerName, String book_copy_id, String title, String author, String genre, String publisher, String datePublished
                String[] data = {fullName, Integer.toString(copy_id), otherBookDetails.get(0),
                        otherBookDetails.get(1), otherBookDetails.get(2), "", ""};

                conn.close();
                view.showMessage("Book borrowed successfully!");

                //generateQRCode(data);
                showQRCodePopup(data);

                loadAvailableBooks();
            } catch (SQLException ex) {
                view.showError(ex.getMessage());
                System.out.println(ex.getMessage());
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    public void addBookCopies() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to add copies");
            return;
        }
        int bookId = view.getSelectedBookID(selectedRow);

        JTextField copiesField = new JTextField(10);
        JButton cancelButton = new JButton("Cancel");
        JButton addButton = new JButton("Add Copies");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Number of copies to add:"));
        inputPanel.add(copiesField);
        panel.add(inputPanel);
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        panel.add(buttonPanel);

        JDialog dialog = new JDialog(view.getFrame(), "Add Book Copies", true);
        dialog.setContentPane(panel);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(view.getFrame());

        // Cancel button action
        cancelButton.addActionListener(e -> {
            dialog.dispose();
        });

        // Add button action
        addButton.addActionListener(e -> {
            if (copiesField.getText().trim().isEmpty() || !copiesField.getText().matches("\\d+")) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid number.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int numberOfCopies = Integer.parseInt(copiesField.getText());
            if (numberOfCopies <= 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a number greater than 0.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            dialog.dispose();
            // Process the valid input
            try {
                String url = System.getenv("LMS_DB_URL");
                Connection conn = DriverManager.getConnection(url);
                MutateBooks.addBookCopy(conn, bookId, numberOfCopies);
                conn.close();
                view.showMessage("Book copies added successfully!");
                loadAvailableBooks();
            } catch (SQLException ex) {
                view.showError("Error adding book copies: " + ex.getMessage());
                System.out.println(ex.getMessage());
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    public void removeBookCopies() {
        int selectedRow = view.getSelectedBookRow();
        if (selectedRow == -1) {
            view.showError("Please select a book to remove copies");
            return;
        }
        int bookId = view.getSelectedBookID(selectedRow);
        int availableCopies = view.getSelectedBookAvailableCopies(selectedRow);

        JTextField copiesField = new JTextField(10);
        JButton cancelButton = new JButton("Cancel");
        JButton addButton = new JButton("Remove Copies");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Number of copies to remove: "));
        inputPanel.add(copiesField);
        panel.add(inputPanel);
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        panel.add(buttonPanel);

        JDialog dialog = new JDialog(view.getFrame(), "Remove Book Copies", true);
        dialog.setContentPane(panel);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(view.getFrame());

        // Cancel button action
        cancelButton.addActionListener(e -> {
            dialog.dispose();
        });

        // Add button action
        addButton.addActionListener(e -> {
            if (copiesField.getText().trim().isEmpty() || !copiesField.getText().matches("\\d+")) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid number.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int numberOfCopies = Integer.parseInt(copiesField.getText());
            if (numberOfCopies <= 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a number greater than 0.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else if (numberOfCopies > availableCopies) {
                JOptionPane.showMessageDialog(dialog,
                        "Number cannot exceed available copies.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            dialog.dispose();
            // Process the valid input
            try {
                String url = System.getenv("LMS_DB_URL");
                Connection conn = DriverManager.getConnection(url);
                MutateBooks.removeBookCopy(conn, bookId, numberOfCopies);
                conn.close();
                view.showMessage("Book copies removed successfully!");
                loadAvailableBooks();
            } catch (SQLException ex) {
                view.showError("Error removing book copies: " + ex.getMessage());
                System.out.println(ex.getMessage());
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    /**
     * Show QR code in a popup dialog
     */
    private void showQRCodePopup(String[] data) {
        try {
            // Format QR content
            String qrContent = formatQRContent(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);

            // Generate QR image
            BufferedImage qrImage = generateQRCodeImage(qrContent);

            // Create popup dialog
            JDialog dialog = new JDialog(view.getFrame(), "Book Borrowed Successfully", true);
            dialog.setLayout(new BorderLayout(10, 10));

            // Add title
            JLabel titleLabel = new JLabel("Book Borrowed Successfully", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            dialog.add(titleLabel, BorderLayout.NORTH);

            // Add QR code image
            JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
            qrLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JPanel qrPanel = new JPanel(new BorderLayout());
            qrPanel.add(qrLabel, BorderLayout.CENTER);

            // Add borrower and book info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            infoPanel.add(new JLabel("Borrower: " + data[0]));
            infoPanel.add(new JLabel("Book: " + data[2]));
            infoPanel.add(new JLabel("Copy ID: " + data[1]));
            qrPanel.add(infoPanel, BorderLayout.SOUTH);

            dialog.add(qrPanel, BorderLayout.CENTER);

            // Add buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

            JButton saveButton = new JButton("Save QR Code");
            saveButton.addActionListener(e -> {
                saveQRImageToFile(qrImage);
                dialog.dispose();
            });
            buttonPanel.add(saveButton);

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeButton);

            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Display dialog
            dialog.pack();
            dialog.setLocationRelativeTo(view.getFrame());
            dialog.setVisible(true);

        } catch (WriterException e) {
            view.showError("Failed to generate QR code: " + e.getMessage());
        }
    }

    /**
     * Save QR code image to file
     */
    private void saveQRImageToFile(BufferedImage qrImage) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save QR Code");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Files", "png"));

        if (fileChooser.showSaveDialog(view.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }

            try {
                ImageIO.write(qrImage, "png", file);
                view.showMessage("QR code saved successfully!");
            } catch (IOException ex) {
                view.showError("Error saving QR code: " + ex.getMessage());
            }
        }
    }
}