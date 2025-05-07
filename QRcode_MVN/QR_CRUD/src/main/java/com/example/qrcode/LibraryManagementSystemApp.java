package com.example.qrcode;
import javax.swing.SwingUtilities;

public class LibraryManagementSystemApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddBook view = new AddBook();
            QRCodeController controller = new QRCodeController(view);
            view.setController(controller);
            view.show();
        });
    }
}
