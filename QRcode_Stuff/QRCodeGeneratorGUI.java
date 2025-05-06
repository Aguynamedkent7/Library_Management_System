import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRCodeGeneratorGUI {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QRCodeGeneratorGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("QR Code Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        
        JPanel panel = new JPanel(new BorderLayout());

        JTextField inputField = new JTextField();
        JButton generateButton = new JButton("Generate QR Code");

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(generateButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);

        generateButton.addActionListener((ActionEvent e) -> {
            String data = inputField.getText();
            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter text to encode.");
                return;
            }

            try {
                String filePath = "qrcode.png";
                generateQRCodeImage(data, filePath);
                JOptionPane.showMessageDialog(frame, "QR Code generated and saved as: " + filePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private static void generateQRCodeImage(String text, String filePath)
            throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }
}
