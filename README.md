# 🏫 Sistem Informasi Koperasi Sekolah (Java Swing)

Selamat datang di repositori **Sistem Informasi Koperasi Sekolah**. Aplikasi ini adalah proyek Tugas Besar Pemrograman Berorientasi Objek (PBO) yang dirancang untuk mendigitalisasi dan mempermudah operasional harian di lingkungan koperasi sekolah.

Aplikasi ini dibangun menggunakan **Java Swing** dengan pendekatan *Object-Oriented Programming* (OOP) murni, menampilkan antarmuka UI yang modern, interaktif, dan terintegrasi penuh dengan *database* **MySQL**.

---

## ✨ Fitur Utama

- 👥 **Manajemen Anggota Siswa** - Pendaftaran, pengubahan, dan penonaktifan data anggota siswa koperasi secara mudah.
- 📦 **Kelola Inventaris Barang** - Pencatatan stok barang, pengaturan harga, serta histori penambahan stok (*restock*) secara *real-time*.
- 💰 **Sistem Tabungan Siswa** - Transaksi setor dan tarik saldo tabungan siswa dengan pencatatan histori transaksi yang aman.
- 🛒 **Toko Koperasi (Sistem Kasir/POS)** - Fitur keranjang belanja interaktif layaknya kasir minimarket. Mendukung perlindungan limit stok dan 2 opsi metode pembayaran:
  - **Tunai (Cash)**
  - **Potong Saldo Tabungan** (Otomatis mendebit saldo khusus Siswa Anggota)
- 🖨️ **Cetak Struk** - Terintegrasi dengan fitur pencetakan struk kasir (*thermal receipt*) untuk bukti transaksi belanja dan tabungan.
- 📊 **Dashboard Interaktif** - Menampilkan ringkasan statistik jumlah anggota, total aset tabungan, omzet penjualan harian, dan log aktivitas terbaru.

---

## 🛠️ Teknologi & Library

Proyek ini dibangun secara *native* menggunakan ekosistem Java:
- **Bahasa Pemrograman:** Java (Mendukung JDK 8 ke atas)
- **GUI Framework:** Java AWT & Swing + [FlatLaf](https://www.formdev.com/flatlaf/) (Flat Light Look and Feel)
- **Database:** MySQL (Lokal via XAMPP / Laragon)
- **Database Driver:** MySQL Connector/J
- **Connection Pool:** HikariCP (Untuk performa koneksi SQL yang optimal dan stabil)

---

## 🚀 Panduan Penggunaan & Instalasi

Untuk mulai menjalankan atau menguji coba aplikasi ini secara lokal di komputer Anda, kami telah menyediakan dokumentasi terpisah:

1. 📂 **[Panduan Setup Database](PANDUAN_DATABASE.md)**
   Baca ini terlebih dahulu untuk mengetahui cara menyalakan server MySQL, sinkronisasi kredensial *localhost*, dan proses *auto-create* tabel database.

2. 📖 **[Panduan Penggunaan Aplikasi (User Manual)](PANDUAN_PENGGUNAAN.md)**
   Berisi petunjuk langkah demi langkah mengenai cara masuk ke sistem (Login), mengoperasikan halaman kasir, mengelola barang, hingga mencetak struk.

---

## 👨‍💻 Tim Pengembang

Tugas Besar Pemrograman Berorientasi Objek (PBO)
**Universitas Pasundan - Fakultas Teknik - Program Studi Teknik Informatika (2026)**

**Disusun Oleh:**
- **Muhamad Nur Salam** (Ketua) - 243040083
- **Grisvi Taus We** (Anggota) - 243040086

**Dosen Pengampu:** 
ADE SUKENDAR, ST.,MT 

---

<div align="center">
  <i>Dirancang dengan ♥ untuk memenuhi standar Pemrograman Berorientasi Objek tingkat lanjut.</i>
</div>
