package com.example.LibraryManagementSystem;

import api.MutateBooks;
import api.Query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import models.Account;
import models.Book;
import models.BorrowedBook;

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

    /**
     * Open QR code reader to scan and process books
     * This method replaces both the old scanQR and returnBook functions
     */
    public void returnBook() {
        ManageBooksUI parentFrame = view;
        // show dialog
        showReferenceOrQRDialog(view.getFrame(),
                this::initializeQRScanner,
                bookCopyID -> {
                    returnBookByBookCopyID(Integer.parseInt(bookCopyID));
                });
    }

    private void returnBookByBookCopyID(int bookCopyID) {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);
            MutateBooks.ReturnBook(conn, bookCopyID);
            view.showMessage("Book returned successfully!");
        } catch (SQLException e) {
            view.showError(e.getMessage());
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

    private void initializeQRScanner() {
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

    public void generateQRCode(int selectedRow) {
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

    public void saveQRCodeToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save QR Code");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        if (fileChooser.showSaveDialog(view.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Add .png extension if not present
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getParentFile(), file.getName() + ".png");
            }

            try {
                // Convert ImageIcon to BufferedImage
                ImageIcon icon = (ImageIcon) view.getQrCodeLabel().getIcon();
                BufferedImage image = new BufferedImage(
                        icon.getIconWidth(),
                        icon.getIconHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g2d = image.createGraphics();
                g2d.drawImage(icon.getImage(), 0, 0, null);
                g2d.dispose();

                // Save the image
                ImageIO.write(image, "png", file);
                JOptionPane.showMessageDialog(view.getFrame(),
                        "QR Code saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                view.showError("Error saving QR code: " + ex.getMessage());
            }
        }
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
                MutateBooks.BorrowBook(conn, firstName, lastName, bookId, returnDate);
                conn.close();
                view.showMessage("Book borrowed successfully!");
                loadAvailableBooks();
            } catch (SQLException ex) {
                view.showError(ex.getMessage());
                System.out.println(ex.getMessage());
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
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

    public void showReferenceOrQRDialog(JFrame parentFrame, Runnable onScanQR, java.util.function.Consumer<String> onBookCopyIdEntered) {
        JButton scanQRButton = new JButton("Scan QR Code");
        JButton ReturnButton = new JButton("Return Book");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Select a row or scan QR Code to return."));
        buttonPanel.add(scanQRButton);
        buttonPanel.add(ReturnButton);
        panel.add(buttonPanel);

        JDialog dialog = new JDialog(parentFrame, "Return Book", true);
        dialog.setContentPane(panel);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(parentFrame);

        // Scan QR code button action
        scanQRButton.addActionListener(e -> {
            dialog.dispose();
            onScanQR.run();
        });

        ReturnButton.addActionListener(e -> {
            dialog.dispose();
            int selectedRow = view.getSelectedBookRow();
            if (selectedRow == -1) {
                view.showError("Please select a book to return");
                return;
            }
            int bookCopyID = view.getSelectedRowBookCopyID(selectedRow);

            try {
                String url = System.getenv("LMS_DB_URL");
                Connection conn = DriverManager.getConnection(url);
                MutateBooks.ReturnBook(conn, bookCopyID);
                conn.close();
                view.showMessage("Book returned successfully!");
                loadBorrowedBooks();
            } catch (SQLException ex) {
                view.showError("Error returning book: " + ex.getMessage());
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
}