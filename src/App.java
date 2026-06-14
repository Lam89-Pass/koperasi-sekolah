import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import ui.MainFrame;
import util.DBHelper;

public class App {
    public static void main(String[] args) {
        System.out.println("Memulai aplikasi Koperasi Sekolah...");
        
        try {
            DBHelper.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Gagal menginisialisasi database: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Gagal terhubung ke MySQL! Pastikan MySQL (Laragon/XAMPP) sudah aktif.\nError: " + e.getMessage(), 
                "Kesalahan Koneksi Database", 
                JOptionPane.ERROR_MESSAGE);
        }

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            System.out.println("Tema FlatLaf berhasil dimuat.");
        } catch (Exception e) {
            System.err.println("Gagal memuat tema FlatLaf. Menggunakan tema default Java Swing.");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                ui.LoginFrame frame = new ui.LoginFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Terjadi kesalahan saat membuka jendela utama: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
