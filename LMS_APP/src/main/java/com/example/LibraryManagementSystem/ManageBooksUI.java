package com.example.LibraryManagementSystem;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class ManageBooksUI {
    private JFrame frame;
    private JTable bookTable;
    private JTextField titleField;
    private JTextField authorField;
    private JList<JCheckBox> genreList;
    private JScrollPane genreScrollPane;
    private JTextField publisherField;
    private JTextField datePublishedField;
    private JLabel qrCodeLabel;
    private ManageBooksFunction controller = new ManageBooksFunction(this);

    public ManageBooksUI() {
        initializeUI();
    }

    private void initializeGenres() {
        DefaultListModel<JCheckBox> model = new DefaultListModel<>();
        JList<String> tempList = controller.getGenres();
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
        genreScrollPane.setPreferredSize(new Dimension(200, 100));
    }


    private void initializeUI() {
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1080);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table panel
        String[] columnNames = {"Title", "Author", "Genre", "Publisher", "Date Published"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bookTable);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        formPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        formPanel.add(authorField);

        initializeGenres();
        formPanel.add(new JLabel("Genre:"));
        formPanel.add(genreScrollPane);

        formPanel.add(new JLabel("Publisher:"));
        publisherField = new JTextField();
        formPanel.add(publisherField);

        formPanel.add(new JLabel("Date Published:"));
        datePublishedField = new JTextField();
        formPanel.add(datePublishedField);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5)); // Changed to 6 rows since we removed one button
        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(e -> controller.addBook());

        JButton editButton = new JButton("Edit Book");
        editButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow >= 0) {
                String[] bookData = getBookAtRow(selectedRow);
                titleField.setText(bookData[0]);
                authorField.setText(bookData[1]);
                setGenreSelection(bookData[2]);
                publisherField.setText(bookData[3]);
                datePublishedField.setText(bookData[4]);
            }
        });

        JButton updateButton = new JButton("Update Book");
        updateButton.addActionListener(e -> controller.updateBook());

        JButton deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(e -> controller.deleteBook());

        JButton returnBookButton = new JButton("Return a Book");
        returnBookButton.addActionListener(e -> controller.returnBook());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> controller.goBack());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(returnBookButton);
        buttonPanel.add(backButton);

        // QR Code panel
        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(JLabel.CENTER);
        qrCodeLabel.setPreferredSize(new Dimension(300, 300));
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBorder(BorderFactory.createTitledBorder("Book QR Code"));
        qrPanel.add(qrCodeLabel, BorderLayout.CENTER);

        // Right panel for QR code and buttons
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(qrPanel, BorderLayout.NORTH);
        rightPanel.add(buttonPanel, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(350, 0));

        // Center panel for table and form
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Main layout
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        frame.add(mainPanel);
    }

    // Rest of the methods remain unchanged...
    public void setController(ManageBooksFunction controller) {
        this.controller = controller;
    }

    public void show() {
        frame.setVisible(true);
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
        return datePublishedField.getText();
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
        datePublishedField.setText("");
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    public void addBookToTable(String[] bookData) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        model.addRow(bookData);
        selectLastRow();
    }

    public int getSelectedBookRow() {
        return bookTable.getSelectedRow();
    }

    public String[] getBookAtRow(int row) {
        DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
        String[] bookData = new String[5];
        for (int i = 0; i < 5; i++) {
            bookData[i] = model.getValueAt(row, i).toString();
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

    public JFrame getFrame() {
        return frame;
    }

    public JTable getBookTable() {
        return bookTable;
    }

    public void selectLastRow() {
        int lastRow = bookTable.getRowCount() - 1;
        if (lastRow >= 0) {
            bookTable.setRowSelectionInterval(lastRow, lastRow);
        }
    }

    public void displayQRCode(ImageIcon qrImage) {
        qrCodeLabel.setIcon(qrImage);
        frame.revalidate();
        frame.repaint();
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