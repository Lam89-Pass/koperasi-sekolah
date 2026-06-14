CREATE DATABASE IF NOT EXISTS koperasi_sekolah;
USE koperasi_sekolah;

-- Tabel Siswa
CREATE TABLE IF NOT EXISTS siswa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nis VARCHAR(20) UNIQUE NOT NULL,
    nama VARCHAR(100) NOT NULL,
    kelas VARCHAR(20) NOT NULL,
    saldo_tabungan DOUBLE DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabel Barang
CREATE TABLE IF NOT EXISTS barang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_barang VARCHAR(50) UNIQUE NOT NULL,
    nama_barang VARCHAR(100) NOT NULL,
    harga DOUBLE NOT NULL,
    stok INT DEFAULT 0
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

-- Seed Data
INSERT INTO siswa (nis, nama, kelas, saldo_tabungan) VALUES 
('2026001', 'Budi Santoso', 'XI-RPL-1', 150000.0),
('2026002', 'Siti Rahmawati', 'X-TKJ-2', 250000.0),
('2026003', 'Andi Wijaya', 'XII-MM-1', 50000.0)
ON DUPLICATE KEY UPDATE nis=nis;

INSERT INTO barang (kode_barang, nama_barang, harga, stok) VALUES
('BRG004', 'Penghapus Joyko', 2500, 80),
('BRG005', 'Rautan Pensil', 3000, 70),
('BRG006', 'Penggaris 30 cm', 5000, 45),
('BRG007', 'Spidol Snowman', 9000, 50),
('BRG008', 'Lem Kertas Fox', 7000, 40),
('BRG009', 'Map Plastik', 5000, 60),
('BRG010', 'Kertas HVS A4 1 Rim', 65000, 20),
('BRG011', 'Dasi Sekolah', 15000, 30),
('BRG012', 'Topi Sekolah', 25000, 20),
('BRG013', 'Kaos Kaki Putih', 12000, 50),
('BRG014', 'Name Tag Sekolah', 10000, 40),
('BRG015', 'Baju Olahraga Sekolah', 85000, 25);
