package com.example.LibraryManagementSystem;
import javax.swing.*;
import java.awt.*;

public class AdminBB extends JFrame {
    private static final Color BG_COLOR = new Color(0xaa, 0xaa, 0xaa);
    private static final Color RESULTS_COLOR = new Color(0xef, 0xef, 0xef);

    public AdminBB() {
        setTitle("Borrowed Books");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Left Panel (Search)
        JPanel searchPanel = createSearchPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // Fixed width
        gbc.weighty = 1;
        mainPanel.add(searchPanel, gbc);

        // Right Panel (Results)
        JPanel resultsPanel = createResultsPanel();
        gbc.gridx = 1;
        gbc.weightx = 1; // Takes remaining space
        mainPanel.add(resultsPanel, gbc);

        add(mainPanel);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, 700));
        panel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Search Components
        JLabel titleLabel = new JLabel("Search book by Title");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JTextField titleField = new JTextField(20);
        gbc.gridy = 1;
        panel.add(titleField, gbc);

        JLabel borrowerLabel = new JLabel("Search book by Borrower");
        gbc.gridy = 2;
        panel.add(borrowerLabel, gbc);

        JTextField borrowerField = new JTextField(20);
        gbc.gridy = 3;
        panel.add(borrowerField, gbc);

        JButton searchButton = new JButton("Search");
        gbc.gridy = 4;
        panel.add(searchButton, gbc);

        // Back Button at bottom
        JButton backButton = new JButton("Back");
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        panel.add(backButton, gbc);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(RESULTS_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Example Table (replace with actual data)
        String[] columns = {"Book Title", "Borrower", "Due Date"};
        Object[][] data = {
                {"The Great Gatsby", "John Doe", "2023-12-01"},
                {"1984", "Jane Smith", "2023-12-05"}
        };

        JTable resultsTable = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminBB().setVisible(true));
    }
}