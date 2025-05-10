package com.example.LibraryManagementSystem;
import javax.swing.*;
import java.awt.*;


public class AdminDashboard extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(0xaa, 0xaa, 0xaa);
    private static final Color CARD_COLOR = new Color(0xef, 0xef, 0xef);

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add logout button at top right
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Add dashboard cards
        mainPanel.add(createDashboardPanel(), BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerPanel.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        GridBagConstraints cns = new GridBagConstraints();
        cns.anchor = GridBagConstraints.CENTER;


        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(100, 30));
        logoutButton.addActionListener(e -> System.exit(0));

        headerPanel.add(titleLabel);
        headerPanel.add(logoutButton);

        return headerPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        dashboardPanel.setBackground(BACKGROUND_COLOR);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 400, 50));

        String[] cardLabels = {
                "Manage Books",
                "Borrowed Books",
                "Returned Books",
                "Manage Students"
        };

        for (String label : cardLabels) {
            JPanel card = createDashboardCard(label);

            if (label.equals("Manage Books")) {
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        // Launch ManageBooksUI via LibraryManagementSystemApp logic
                        SwingUtilities.invokeLater(() -> {
                            ManageBooksUI view = new ManageBooksUI();
                            ManageBooksFunction controller = new ManageBooksFunction(view);
                            view.setController(controller);
                            view.show();
                        });
                    }
                });
            }

            dashboardPanel.add(card);
        }

        return dashboardPanel;
    }

    private JPanel createDashboardCard(String text) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Add hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(CARD_COLOR.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(CARD_COLOR);
            }
        });

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.BELOW_BASELINE;
        card.add(label, gbc);

        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdminDashboard().setVisible(true);
        });
    }
}