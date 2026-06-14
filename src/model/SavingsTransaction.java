package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class SavingsTransaction extends Transaction implements PrintableReceipt {
    private String transactionType;
    private String description;
    private String studentName; 
    private String studentNis;

    public SavingsTransaction() {}

    public SavingsTransaction(int studentId, String transactionType, double amount, String description) {
        super(studentId, amount);
        this.transactionType = transactionType;
        this.description = description;
    }

    public SavingsTransaction(int id, int studentId, String transactionType, double amount, Timestamp timestamp, String description) {
        super(id, studentId, amount, timestamp);
        this.transactionType = transactionType;
        this.description = description;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStudentDetails(String name, String nis) {
        this.studentName = name;
        this.studentNis = nis;
    }

    @Override
    public boolean processTransaction(Connection conn) throws SQLException {
        if ("TARIK".equalsIgnoreCase(transactionType)) {
            String checkSql = "SELECT saldo_tabungan, nama, nis FROM siswa WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, studentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        double balance = rs.getDouble("saldo_tabungan");
                        this.studentName = rs.getString("nama");
                        this.studentNis = rs.getString("nis");
                        if (balance < amount) {
                            throw new SQLException("Saldo tidak mencukupi untuk penarikan! Saldo saat ini: Rp" + balance);
                        }
                    } else {
                        throw new SQLException("Siswa tidak ditemukan!");
                    }
                }
            }
        } else {
            String nameSql = "SELECT nama, nis FROM siswa WHERE id = ?";
            try (PreparedStatement nameStmt = conn.prepareStatement(nameSql)) {
                nameStmt.setInt(1, studentId);
                try (ResultSet rs = nameStmt.executeQuery()) {
                    if (rs.next()) {
                        this.studentName = rs.getString("nama");
                        this.studentNis = rs.getString("nis");
                    }
                }
            }
        }

        String insertSql = "INSERT INTO transaksi_tabungan (siswa_id, jenis_transaksi, jumlah, tanggal, keterangan) VALUES (?, ?, ?, ?, ?)";
        int transactionId = -1;
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, studentId);
            insertStmt.setString(2, transactionType.toUpperCase());
            insertStmt.setDouble(3, amount);
            insertStmt.setTimestamp(4, timestamp);
            insertStmt.setString(5, description);

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.id = generatedKeys.getInt(1);
                }
            }
        }

        String updateSql;
        if ("SIMPAN".equalsIgnoreCase(transactionType)) {
            updateSql = "UPDATE siswa SET saldo_tabungan = saldo_tabungan + ? WHERE id = ?";
        } else {
            updateSql = "UPDATE siswa SET saldo_tabungan = saldo_tabungan - ? WHERE id = ?";
        }

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setDouble(1, amount);
            updateStmt.setInt(2, studentId);
            return updateStmt.executeUpdate() > 0;
        }
    }

    @Override
    public String generateReceiptText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        StringBuilder receipt = new StringBuilder();
        receipt.append("========================================\n");
        receipt.append("          KOPERASI SEKOLAH              \n");
        receipt.append("       STRUK TRANSAKSI TABUNGAN         \n");
        receipt.append("========================================\n");
        receipt.append("ID Transaksi : TAB-").append(id).append("\n");
        receipt.append("Tanggal      : ").append(sdf.format(timestamp)).append("\n");
        receipt.append("Siswa        : ").append(studentName).append("\n");
        receipt.append("NIS          : ").append(studentNis).append("\n");
        receipt.append("Jenis        : ").append(transactionType).append("\n");
        receipt.append("Jumlah       : Rp").append(String.format("%,.2f", amount)).append("\n");
        receipt.append("Keterangan   : ").append(description == null || description.trim().isEmpty() ? "-" : description).append("\n");
        receipt.append("========================================\n");
        receipt.append("            TERIMA KASIH                \n");
        receipt.append("========================================\n");
        return receipt.toString();
    }
}

