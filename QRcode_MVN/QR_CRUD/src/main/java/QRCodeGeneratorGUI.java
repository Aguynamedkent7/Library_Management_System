import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class QRCodeGeneratorGUI {
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font FIELD_FONT = new Font("Arial", Font.PLAIN, 14);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QRCodeGeneratorGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Advanced QR Code Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(390, 575);
        frame.setResizable(false);

        // Main panel with BorderLayout
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create form panel for input fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Define fields with labels
        Map<String, JTextField> inputFields = new LinkedHashMap<>();
        inputFields.put("Book Title:", new JTextField(20));
        inputFields.put("Author:", new JTextField(20));
        inputFields.put("Genre:", new JTextField(20));
        inputFields.put("Date Published:", new JTextField(20));

        // Add fields to form panel
        gbc.gridy = 0;
        for (Map.Entry<String, JTextField> entry : inputFields.entrySet()) {
            gbc.gridx = 0;
            JLabel label = new JLabel(entry.getKey());
            label.setFont(LABEL_FONT);
            formPanel.add(label, gbc);

            gbc.gridx = 1;
            JTextField field = entry.getValue();
            field.setFont(FIELD_FONT);
            formPanel.add(field, gbc);

            gbc.gridy++;
        }

        // QR code display panel
        JLabel qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(JLabel.CENTER);
        qrCodeLabel.setVerticalAlignment(JLabel.CENTER);
        qrCodeLabel.setPreferredSize(new Dimension(QR_CODE_WIDTH, QR_CODE_HEIGHT));
        JPanel qrCodePanel = new JPanel(new BorderLayout());
        qrCodePanel.add(qrCodeLabel, BorderLayout.CENTER);
        qrCodePanel.setBorder(BorderFactory.createTitledBorder("Generated QR Code"));

        // Generate button
        JButton generateButton = new JButton("Generate QR Code");
        generateButton.setPreferredSize(new Dimension(200, 40));
        generateButton.setFont(LABEL_FONT);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(generateButton);

        // Add components to main panel
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(qrCodePanel, BorderLayout.SOUTH);

        // Generate button action
        generateButton.addActionListener((ActionEvent e) -> {
            StringBuilder qrContent = new StringBuilder();
            qrContent.append("=== Book Information ===\n\n");

            for (Map.Entry<String, JTextField> entry : inputFields.entrySet()) {
                String label = entry.getKey().replace(":", "");
                String value = entry.getValue().getText().trim();
                if (!value.isEmpty()) {
                    qrContent.append(String.format("%-10s: %s\n", label, value));
                }
            }

            if (qrContent.length() <= 20) { // Just headers and whitespace
                JOptionPane.showMessageDialog(frame, "Please enter at least one field");
                return;
            }

            try {
                BufferedImage qrImage = generateQRCodeImage(qrContent.toString());
                qrCodeLabel.setIcon(new ImageIcon(qrImage));
                frame.pack();
            } catch (WriterException ex) {
                JOptionPane.showMessageDialog(frame, "Error generating QR code: " + ex.getMessage());
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static BufferedImage generateQRCodeImage(String text) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                QR_CODE_WIDTH,
                QR_CODE_HEIGHT);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}