package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;

public class ShopTransaction extends Transaction implements PrintableReceipt {
    private String paymentMethod; 
    private Map<Product, Integer> items; 
    private String studentName = "Umum (Bukan Anggota)";
    private String studentNis = "-";

    public ShopTransaction() {}

    public ShopTransaction(Integer studentId, double totalAmount, String paymentMethod, Map<Product, Integer> items) {
        super(studentId, totalAmount);
        this.paymentMethod = paymentMethod;
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Map<Product, Integer> getItems() {
        return items;
    }

    public void setItems(Map<Product, Integer> items) {
        this.items = items;
    }

    @Override
    public boolean processTransaction(Connection conn) throws SQLException {
        if ("TABUNGAN".equalsIgnoreCase(paymentMethod)) {
            if (studentId == null) {
                throw new SQLException("Transaksi dengan tabungan memerlukan anggota siswa!");
            }

            String checkSql = "SELECT saldo_tabungan, nama, nis FROM siswa WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, studentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        double balance = rs.getDouble("saldo_tabungan");
                        this.studentName = rs.getString("nama");
                        this.studentNis = rs.getString("nis");
                        if (balance < amount) {
                            throw new SQLException("Saldo tabungan siswa tidak cukup! Saldo: Rp" + balance + ", Pembelian: Rp" + amount);
                        }
                    } else {
                        throw new SQLException("Siswa tidak ditemukan!");
                    }
                }
            }
        } else if (studentId != null) {
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

        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();

            String stockSql = "SELECT stok, nama_barang FROM barang WHERE id = ?";
            try (PreparedStatement stockStmt = conn.prepareStatement(stockSql)) {
                stockStmt.setInt(1, product.getId());
                try (ResultSet rs = stockStmt.executeQuery()) {
                    if (rs.next()) {
                        int stock = rs.getInt("stok");
                        String name = rs.getString("nama_barang");
                        if (stock < quantity) {
                            throw new SQLException("Stok barang '" + name + "' tidak mencukupi! Tersisa: " + stock);
                        }
                    } else {
                        throw new SQLException("Barang dengan ID " + product.getId() + " tidak ditemukan!");
                    }
                }
            }
        }

        if ("TABUNGAN".equalsIgnoreCase(paymentMethod)) {
            String updateBalanceSql = "UPDATE siswa SET saldo_tabungan = saldo_tabungan - ? WHERE id = ?";
            try (PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceSql)) {
                updateBalanceStmt.setDouble(1, amount);
                updateBalanceStmt.setInt(2, studentId);
                updateBalanceStmt.executeUpdate();
            }

            String insertTabunganSql = "INSERT INTO transaksi_tabungan (siswa_id, jenis_transaksi, jumlah, tanggal, keterangan) VALUES (?, 'TARIK', ?, ?, 'Pembelian Toko (Potong Saldo)')";
            try (PreparedStatement insertTabStmt = conn.prepareStatement(insertTabunganSql)) {
                insertTabStmt.setInt(1, studentId);
                insertTabStmt.setDouble(2, amount);
                insertTabStmt.setTimestamp(3, timestamp);
                insertTabStmt.executeUpdate();
            }
        }

        String insertShopSql = "INSERT INTO transaksi_toko (siswa_id, total_harga, metode_pembayaran, tanggal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertShopStmt = conn.prepareStatement(insertShopSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            if (studentId == null) {
                insertShopStmt.setNull(1, Types.INTEGER);
            } else {
                insertShopStmt.setInt(1, studentId);
            }
            insertShopStmt.setDouble(2, amount);
            insertShopStmt.setString(3, paymentMethod.toUpperCase());
            insertShopStmt.setTimestamp(4, timestamp);

            int affectedRows = insertShopStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Gagal mencatat transaksi toko.");
            }

            try (ResultSet generatedKeys = insertShopStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.id = generatedKeys.getInt(1);
                }
            }
        }

        String insertDetailSql = "INSERT INTO detail_transaksi_toko (transaksi_toko_id, barang_id, jumlah, harga_satuan, subtotal) VALUES (?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE barang SET stok = stok - ? WHERE id = ?";

        try (PreparedStatement insertDetailStmt = conn.prepareStatement(insertDetailSql);
             PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql)) {

            for (Map.Entry<Product, Integer> entry : items.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                double subtotal = product.getPrice() * quantity;

                insertDetailStmt.setInt(1, this.id);
                insertDetailStmt.setInt(2, product.getId());
                insertDetailStmt.setInt(3, quantity);
                insertDetailStmt.setDouble(4, product.getPrice());
                insertDetailStmt.setDouble(5, subtotal);
                insertDetailStmt.addBatch();

                updateStockStmt.setInt(1, quantity);
                updateStockStmt.setInt(2, product.getId());
                updateStockStmt.addBatch();
            }

            insertDetailStmt.executeBatch();
            updateStockStmt.executeBatch();
        }

        return true;
    }

    @Override
    public String generateReceiptText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        StringBuilder receipt = new StringBuilder();
        receipt.append("========================================\n");
        receipt.append("          KOPERASI SEKOLAH              \n");
        receipt.append("            STRUK BELANJA               \n");
        receipt.append("========================================\n");
        receipt.append("ID Transaksi : TOKO-").append(id).append("\n");
        receipt.append("Tanggal      : ").append(sdf.format(timestamp)).append("\n");
        receipt.append("Pelanggan    : ").append(studentName).append("\n");
        receipt.append("NIS          : ").append(studentNis).append("\n");
        receipt.append("Pembayaran   : ").append(paymentMethod).append("\n");
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("%-18s %3s %8s %9s\n", "Barang", "Qty", "Harga", "Subtotal"));
        receipt.append("----------------------------------------\n");

        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product product = entry.getKey();
            int qty = entry.getValue();
            double sub = product.getPrice() * qty;

            String name = product.getName();
            if (name.length() > 18) {
                name = name.substring(0, 15) + "...";
            }
            receipt.append(String.format("%-18s %3d %8.0f %9.0f\n", 
                name, qty, product.getPrice(), sub));
        }
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("TOTAL        : Rp%,.2f\n", amount));
        receipt.append("========================================\n");
        receipt.append("      Barang yang sudah dibeli tidak    \n");
        receipt.append("         dapat ditukar/kembalikan       \n");
        receipt.append("            TERIMA KASIH                \n");
        receipt.append("========================================\n");
        return receipt.toString();
    }
}

