CREATE DATABASE IF NOT EXISTS koperasi_sekolah;
USE koperasi_sekolah;

-- Tabel Users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabel Siswa
CREATE TABLE IF NOT EXISTS siswa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nis VARCHAR(20) UNIQUE NOT NULL,
    nama VARCHAR(100) NOT NULL,
    kelas VARCHAR(20) NOT NULL,
    saldo_tabungan DOUBLE DEFAULT 0.0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabel Barang
CREATE TABLE IF NOT EXISTS barang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_barang VARCHAR(50) UNIQUE NOT NULL,
    nama_barang VARCHAR(100) NOT NULL,
    kategori VARCHAR(50) DEFAULT 'Lainnya',
    stok INT DEFAULT 0,
    satuan VARCHAR(20) DEFAULT 'Pcs',
    harga_beli DOUBLE DEFAULT 0.0,
    harga DOUBLE NOT NULL,
    minimum_stok INT DEFAULT 0,
    deskripsi TEXT,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- Tabel Riwayat Restock
CREATE TABLE IF NOT EXISTS riwayat_restock (
    id INT AUTO_INCREMENT PRIMARY KEY,
    barang_id INT NOT NULL,
    jumlah_tambah INT NOT NULL,
    tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    keterangan VARCHAR(255),
    FOREIGN KEY (barang_id) REFERENCES barang(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabel Transaksi Tabungan
CREATE TABLE IF NOT EXISTS transaksi_tabungan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    siswa_id INT NOT NULL,
    jenis_transaksi ENUM('SIMPAN', 'TARIK') NOT NULL,
    jumlah DOUBLE NOT NULL,
    tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    keterangan TEXT,
    FOREIGN KEY (siswa_id) REFERENCES siswa(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabel Transaksi Toko
CREATE TABLE IF NOT EXISTS transaksi_toko (
    id INT AUTO_INCREMENT PRIMARY KEY,
    siswa_id INT NULL,
    total_harga DOUBLE NOT NULL,
    metode_pembayaran ENUM('CASH', 'TABUNGAN') NOT NULL,
    tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (siswa_id) REFERENCES siswa(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Tabel Detail Transaksi Toko
CREATE TABLE IF NOT EXISTS detail_transaksi_toko (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaksi_toko_id INT NOT NULL,
    barang_id INT NOT NULL,
    jumlah INT NOT NULL,
    harga_satuan DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (transaksi_toko_id) REFERENCES transaksi_toko(id) ON DELETE CASCADE,
    FOREIGN KEY (barang_id) REFERENCES barang(id) ON DELETE CASCADE
) ENGINE=InnoDB;
