package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.toedter.calendar.JDateChooser;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;


public class ManageBooksUI {
    private JFrame frame;
    private JTable bookTable;
    private JTextField titleField;
    private JTextField authorField;
    private JList<JCheckBox> genreList;
    private JScrollPane genreScrollPane;
    private JTextField publisherField;
    private JLabel qrCodeLabel;
    private ManageBooksFunction controller = new ManageBooksFunction(this);
    private JDateChooser datePublishedChooser;

    public ManageBooksUI() {
        initializeUI();
    }

    private void initializeGenres() {
        DefaultListModel<JCheckBox> model = new DefaultListModel<>();
        JList<String> tempList = controller.queryGenresFromDB();
        ListModel<String> tempModel = tempList.getModel();

        for (int i = 0; i < tempModel.getSize(); i++) {
            model.addElement(new JCheckBox(tempModel.getElementAt(i)));
        }

        genreList = new JList<>(model);
        genreList.setCellRenderer(new CheckBoxListCellRenderer());
        genreList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Handle checkbox toggling
        genreList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int index = genreList.locationToIndex(event.getPoint());
                if (index >= 0) {
                    JCheckBox checkbox = genreList.getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    genreList.repaint();
                }
            }
        });

        genreScrollPane = new JScrollPane(genreList);
        genreScrollPane.setPreferredSize(new Dimension(150, 30));
    }


    private void initializeUI() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Left Panel: Vertical Action Buttons (Add/Edit/Update/Delete)
        JPanel leftPanel = new JPanel(new GridLayout(5, 1, 5, 5)); // Changed from 4 to 5 rows
        JButton addButton = new JButton("Add Book");
        JButton editButton = new JButton("Edit Book");
        editButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow >= 0) {
                Object[] bookData = getBookAtRow(selectedRow);
                // bookData[0] contains book ID
                titleField.setText(bookData[1].toString());
                authorField.setText(bookData[2].toString());
                setGenreSelection(bookData[3].toString()); // Critical for genre checkboxes
                publisherField.setText(bookData[4].toString());
                setDatePublished(bookData[5].toString());
            } else {
                showError("Please select a book to edit!");
            }
        });
        JButton updateButton = new JButton("Update Book");
        JButton deleteButton = new JButton("Delete Book");
        JButton generateQRButton = new JButton("Generate QR Code");

        leftPanel.add(addButton);
        leftPanel.add(editButton);
        leftPanel.add(updateButton);
        leftPanel.add(deleteButton);
        leftPanel.add(generateQRButton);

        // Add action listener for the Generate QR Code button
        generateQRButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow >= 0) {
                controller.generateQRCode(selectedRow);
            } else {
                showError("Please select a book to generate QR code!");
            }
        });
        leftPanel.setPreferredSize(new Dimension(150, 0));
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // 2. Center Panel: Table + Input Fields (Stacked vertically)
        JPanel centerPanel = new JPanel(new BorderLayout());


        String[] columnNames = {"ID", "Title", "Author", "Genre", "Publisher", "Date Published", "Available Copies"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        bookTable = new JTable(model);
        bookTable.setDefaultEditor(Object.class, null);
        bookTable.getTableHeader().setReorderingAllowed(false); // Prevent column reordering
        bookTable.setDragEnabled(false); // Prevent row reordering
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // remove ID column from table
        bookTable.getColumnModel().removeColumn(bookTable.getColumnModel().getColumn(0));
        JScrollPane tableScrollPane = new JScrollPane(bookTable);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        controller.loadAvailableBooks();

        // Input Fields (Under the table)
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 50, 10));

        Font smallFont = new Font("Arial", Font.PLAIN, 12);
        Dimension fieldSize = new Dimension(150, 30);

// Title
        formPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        titleField.setPreferredSize(fieldSize);
        titleField.setFont(smallFont);
        formPanel.add(titleField);

// Author
        formPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        authorField.setPreferredSize(fieldSize);
        authorField.setFont(smallFont);
        formPanel.add(authorField);

// Genre
        initializeGenres(); // assumes genreScrollPane is created here
        formPanel.add(new JLabel("Genre:"));
        formPanel.add(genreScrollPane);

// Publisher
        formPanel.add(new JLabel("Publisher:"));
        publisherField = new JTextField();
        publisherField.setPreferredSize(fieldSize);
        publisherField.setFont(smallFont);
        formPanel.add(publisherField);

// Date Published
        formPanel.add(new JLabel("Date Published:"));
        datePublishedChooser = new JDateChooser();
        datePublishedChooser.setPreferredSize(fieldSize);
        datePublishedChooser.setFont(smallFont);
        datePublishedChooser.setDateFormatString("yyyy-MM-dd");
        datePublishedChooser.getDateEditor().getUiComponent().setEnabled(false);
        formPanel.add(datePublishedChooser);

        // Add input fields below the table

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Panel: QR Code (Left) + Return/Back Buttons (Right)
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // QR Code (Left)
        qrCodeLabel = new JLabel();
        qrCodeLabel.setPreferredSize(new Dimension(300, 300));
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBorder(BorderFactory.createTitledBorder("Book QR Code"));
        qrPanel.add(qrCodeLabel, BorderLayout.CENTER);
        bottomPanel.add(qrPanel, BorderLayout.WEST);

        // Create a button panel for the save button
        JPanel qrButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveQRButton = new JButton("Save QR Code");
        qrButtonPanel.add(saveQRButton);
        qrPanel.add(qrButtonPanel, BorderLayout.SOUTH);


        // Return/Back Buttons (Right)
        JPanel returnBackPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton returnButton = new JButton("Return a Book");
        JButton backButton = new JButton("Back");
        returnBackPanel.add(returnButton);
        returnBackPanel.add(backButton);
        bottomPanel.add(formPanel, BorderLayout.CENTER, FlowLayout.CENTER);
        bottomPanel.add(returnBackPanel, BorderLayout.SOUTH);


        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> controller.addBook());

        updateButton.addActionListener(e -> controller.updateBook());
        deleteButton.addActionListener(e -> controller.deleteBook());
        returnButton.addActionListener(e -> controller.returnBook());
        backButton.addActionListener(e -> controller.goBack());
        // Add action listener to the save button
        saveQRButton.addActionListener(e -> {
            if (qrCodeLabel.getIcon() != null) {
                controller.saveQRCodeToFile();
            } else {
                showError("No QR code to save! Generate a QR code first.");
            }
        });


        frame.add(mainPanel);
    }

    // Rest of the methods remain unchanged...
    public void setController(ManageBooksFunction controller) {
        this.controller = controller;
    }

    public void show() {
        frame.setVisible(true);
    }

    public int getSelectedBookID(int selectedRow) {
        // Convert view index to model index in case table is sorted
        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        // Assuming ID is stored in the first column (index 0)
        Object idValue = bookTable.getModel().getValueAt(modelRow, 0);
        return Integer.parseInt(idValue.toString());

    }

    public String getTitle() {
        return titleField.getText();
    }

    public String getAuthor() {
        return authorField.getText();
    }

    public String getGenre() {
        ListModel<JCheckBox> model = genreList.getModel();
        StringBuilder genres = new StringBuilder();
        boolean first = true;

        for (int i = 0; i < model.getSize(); i++) {
            JCheckBox checkbox = model.getElementAt(i);
            if (checkbox.isSelected()) {
                if (!first) {
                    genres.append(", ");
                }
                genres.append(checkbox.getText());
                first = false;
            }
        }
        return genres.toString();
    }

    public void setGenreSelection(String genres) {
        ListModel<JCheckBox> model = genreList.getModel();
        // Clear all selections first
        for (int i = 0; i < model.getSize(); i++) {
            model.getElementAt(i).setSelected(false);
        }

        if (genres == null || genres.trim().isEmpty()) {
            return;
        }

        String[] genreArray = genres.split(",");
        for (String genre : genreArray) {
            String trimmedGenre = genre.trim();
            for (int i = 0; i < model.getSize(); i++) {
                JCheckBox checkbox = model.getElementAt(i);
                if (checkbox.getText().equals(trimmedGenre)) {
                    checkbox.setSelected(true);
                    break;
                }
            }
        }
        genreList.repaint();
    }


    public String getPublisher() {
        return publisherField.getText();
    }

    public String getDatePublished() {
        if (datePublishedChooser.getDate() != null) {
            LocalDate localDate = datePublishedChooser.getDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            return localDate.toString(); // Returns in yyyy-MM-dd format
        }
        return "";
    }

    public void clearForm() {
        titleField.setText("");
        authorField.setText("");
        ListModel<JCheckBox> model = genreList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            model.getElementAt(i).setSelected(false);
        }
        genreList.repaint();
        publisherField.setText("");
        datePublishedChooser.setDate(null);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addBookToTable(Object[] bookData) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.addRow(bookData);
        selectFirstRow();
    }

    public int getSelectedBookRow() {
        return bookTable.getSelectedRow();
    }

    public Object[] getBookAtRow(int row) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        Object[] bookData = new Object[6];
        for (int i = 0; i < 6; i++) {
            bookData[i] = model.getValueAt(row, i);
        }
        return bookData;
    }

    public void updateBookInTable(int row, String[] bookData) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        for (int i = 0; i < 5; i++) {
            model.setValueAt(bookData[i], row, i);
        }
    }

    public void removeBookFromTable(int row) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.removeRow(row);
    }

    public void clearTable() {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.setRowCount(0);
    }


    public JFrame getFrame() {
        return frame;
    }

    public JTable getBookTable() {
        return bookTable;
    }

    public void selectFirstRow() {
        bookTable.setRowSelectionInterval(0, 0);
    }

    public void displayQRCode(ImageIcon qrImage) {
        qrCodeLabel.setIcon(qrImage);
        frame.revalidate();
        frame.repaint();
    }

    public JLabel getQrCodeLabel() {
        return qrCodeLabel;
    }

    public void setDatePublished(String dateStr) {
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                LocalDate localDate = LocalDate.parse(dateStr);
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                datePublishedChooser.setDate(date);
            } catch (DateTimeParseException e) {
                datePublishedChooser.setDate(null);
            }
        } else {
            datePublishedChooser.setDate(null);
        }
    }

    // Add this class at the end of ManageBooksUI
    private static class CheckBoxListCellRenderer extends JCheckBox implements ListCellRenderer<JCheckBox> {
        @Override
        public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value,
                                                    int index, boolean isSelected, boolean cellHasFocus) {
            setSelected(value.isSelected());
            setText(value.getText());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return this;
        }
    }
}