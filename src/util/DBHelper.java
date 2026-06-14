package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBHelper {
    private static String urlServer;
    private static String dbName;
    private static String urlDb;
    private static String user;
    private static String password;

    static {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("File config.properties tidak ditemukan. Menggunakan konfigurasi default (localhost).");
        }

        String host = props.getProperty("db.host", "localhost");
        String port = props.getProperty("db.port", "3306");
        dbName = props.getProperty("db.name", "koperasi_sekolah");
        user = props.getProperty("db.user", "root");
        password = props.getProperty("db.password", "");

        urlServer = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        urlDb = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver tidak ditemukan: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(urlDb, user, password);
    }

    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(urlServer, user, password);
    }

    public static void initializeDatabase() {
        System.out.println("Memulai inisialisasi database...");

        try (Connection conn = getServerConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            System.out.println("Database '" + dbName + "' siap.");

        } catch (SQLException e) {
            System.err.println("Gagal membuat database: " + e.getMessage());
            return;
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "password VARCHAR(255) NOT NULL," +
                    "role VARCHAR(20) DEFAULT 'ADMIN'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB;";
            stmt.execute(createUsersTable);

            String createSiswaTable = "CREATE TABLE IF NOT EXISTS siswa (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "nis VARCHAR(20) UNIQUE NOT NULL," +
                    "nama VARCHAR(100) NOT NULL," +
                    "kelas VARCHAR(20) NOT NULL," +
                    "saldo_tabungan DOUBLE DEFAULT 0.0," +
                    "is_active BOOLEAN DEFAULT TRUE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB;";
            stmt.execute(createSiswaTable);

            String createBarangTable = "CREATE TABLE IF NOT EXISTS barang (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "kode_barang VARCHAR(50) UNIQUE NOT NULL," +
                    "nama_barang VARCHAR(100) NOT NULL," +
                    "kategori VARCHAR(50) DEFAULT 'Lainnya'," +
                    "stok INT DEFAULT 0," +
                    "satuan VARCHAR(20) DEFAULT 'Pcs'," +
                    "harga_beli DOUBLE DEFAULT 0.0," +
                    "harga DOUBLE NOT NULL," +
                    "minimum_stok INT DEFAULT 0," +
                    "deskripsi TEXT," +
                    "is_active BOOLEAN DEFAULT TRUE" +
                    ") ENGINE=InnoDB;";
            stmt.execute(createBarangTable);

            try { stmt.execute("ALTER TABLE siswa ADD COLUMN is_active BOOLEAN DEFAULT TRUE"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE barang ADD COLUMN kategori VARCHAR(50) DEFAULT 'Lainnya'"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE barang ADD COLUMN satuan VARCHAR(20) DEFAULT 'Pcs'"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE barang ADD COLUMN harga_beli DOUBLE DEFAULT 0.0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE barang ADD COLUMN minimum_stok INT DEFAULT 0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE barang ADD COLUMN deskripsi TEXT"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE barang ADD COLUMN is_active BOOLEAN DEFAULT TRUE"); } catch (SQLException e) {}

            String createRiwayatRestockTable = "CREATE TABLE IF NOT EXISTS riwayat_restock (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "barang_id INT NOT NULL," +
                    "jumlah_tambah INT NOT NULL," +
                    "tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "keterangan VARCHAR(255)," +
                    "FOREIGN KEY (barang_id) REFERENCES barang(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB;";
            stmt.execute(createRiwayatRestockTable);

            String createTransaksiTabunganTable = "CREATE TABLE IF NOT EXISTS transaksi_tabungan (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "siswa_id INT NOT NULL," +
                    "jenis_transaksi ENUM('SIMPAN', 'TARIK') NOT NULL," +
                    "jumlah DOUBLE NOT NULL," +
                    "tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "keterangan TEXT," +
                    "FOREIGN KEY (siswa_id) REFERENCES siswa(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB;";
            stmt.execute(createTransaksiTabunganTable);

            String createTransaksiTokoTable = "CREATE TABLE IF NOT EXISTS transaksi_toko (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "siswa_id INT NULL," +
                    "total_harga DOUBLE NOT NULL," +
                    "metode_pembayaran ENUM('CASH', 'TABUNGAN') NOT NULL," +
                    "tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (siswa_id) REFERENCES siswa(id) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;";
            stmt.execute(createTransaksiTokoTable);

            String createDetailTransaksiTokoTable = "CREATE TABLE IF NOT EXISTS detail_transaksi_toko (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "transaksi_toko_id INT NOT NULL," +
                    "barang_id INT NOT NULL," +
                    "jumlah INT NOT NULL," +
                    "harga_satuan DOUBLE NOT NULL," +
                    "subtotal DOUBLE NOT NULL," +
                    "FOREIGN KEY (transaksi_toko_id) REFERENCES transaksi_toko(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (barang_id) REFERENCES barang(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB;";
            stmt.execute(createDetailTransaksiTokoTable);

            System.out.println("Seluruh tabel database berhasil diperiksa/dibuat.");

            seedDataIfEmpty(conn);

        } catch (SQLException e) {
            System.err.println("Gagal membuat tabel database: " + e.getMessage());
        }
    }

    private static void seedDataIfEmpty(Connection conn) throws SQLException {
        boolean isUsersEmpty = true;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                isUsersEmpty = false;
            }
        }

        boolean isSiswaEmpty = true;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM siswa")) {
            if (rs.next() && rs.getInt(1) > 0) {
                isSiswaEmpty = false;
            }
        }

        boolean isBarangEmpty = true;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM barang")) {
            if (rs.next() && rs.getInt(1) > 0) {
                isBarangEmpty = false;
            }
        }

        if (isUsersEmpty) {
            System.out.println("Menanam data dummy admin...");
            String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                ps.setString(3, "ADMIN");
                ps.executeUpdate();
            }
        }

        if (isSiswaEmpty) {
            System.out.println("Menanam data dummy siswa...");
            String insertSiswaSql = "INSERT INTO siswa (nis, nama, kelas, saldo_tabungan) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSiswaSql)) {
                ps.setString(1, "2026001");
                ps.setString(2, "Budi Santoso");
                ps.setString(3, "XI-RPL-1");
                ps.setDouble(4, 150000.0);
                ps.addBatch();

                ps.setString(1, "2026002");
                ps.setString(2, "Siti Rahmawati");
                ps.setString(3, "X-TKJ-2");
                ps.setDouble(4, 250000.0);
                ps.addBatch();

                ps.setString(1, "2026003");
                ps.setString(2, "Andi Wijaya");
                ps.setString(3, "XII-MM-1");
                ps.setDouble(4, 50000.0);
                ps.addBatch();

                ps.executeBatch();
            }
        }

        if (isBarangEmpty) {
            System.out.println("Menanam data dummy barang...");
            String insertBarangSql = "INSERT INTO barang (kode_barang, nama_barang, harga, stok) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBarangSql)) {
                ps.setString(1, "BRG001");
                ps.setString(2, "Buku Tulis Kiky A5");
                ps.setDouble(3, 8500.0);
                ps.setInt(4, 100);
                ps.addBatch();

                ps.setString(1, "BRG002");
                ps.setString(2, "Pulpen Pilot Black 0.5");
                ps.setDouble(3, 6000.0);
                ps.setInt(4, 150);
                ps.addBatch();

                ps.setString(1, "BRG003");
                ps.setString(2, "Pensil Faber Castell 2B");
                ps.setDouble(3, 4500.0);
                ps.setInt(4, 80);
                ps.addBatch();

                ps.setString(1, "BRG004");
                ps.setString(2, "Penggaris Butterfly 30cm");
                ps.setDouble(3, 5000.0);
                ps.setInt(4, 50);
                ps.addBatch();

                ps.setString(1, "BRG005");
                ps.setString(2, "Dasi Abu-Abu SMA");
                ps.setDouble(3, 15000.0);
                ps.setInt(4, 40);
                ps.addBatch();

                ps.executeBatch();
            }
        }
    }
}

