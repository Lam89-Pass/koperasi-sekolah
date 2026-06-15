# Panduan Setup Database Koperasi Sekolah (Local)

Aplikasi Koperasi Sekolah ini menggunakan **MySQL** sebagai sistem basis datanya. Walaupun aplikasi ini sudah dilengkapi dengan fitur *Auto-Create Table* (tabel akan dibuat otomatis jika belum ada), Anda tetap perlu memastikan server MySQL lokal Anda berjalan.

Berikut adalah panduan lengkap cara menyiapkan database untuk menjalankan aplikasi secara lokal di komputer Anda.

## Persiapan Awal
Pastikan Anda sudah menginstal salah satu aplikasi Web Server lokal berikut:
- **XAMPP**
- **Laragon**
- Atau instalasi **MySQL Server** / **MariaDB** secara langsung.

## Langkah-langkah Menjalankan Database

### 1. Aktifkan MySQL
- Buka **XAMPP Control Panel** (atau Laragon).
- Klik tombol **Start** pada modul **MySQL**. 
- Pastikan indikator MySQL berwarna hijau (berjalan pada port `3306`).

### 2. Konfigurasi Kredensial Database
Aplikasi ini diatur secara *default* untuk terhubung ke MySQL menggunakan konfigurasi standar lokal. 
- **Host**: `localhost`
- **Port**: `3306`
- **Username**: `root`
- **Password**: *(kosong)*
- **Database Name**: `koperasi_sekolah`

Jika MySQL Anda memiliki *password*, Anda wajib mengubahnya di dalam kode sumber `src/util/DBHelper.java` sebelum melakukan kompilasi.

### 3. Buat Database dan Tabel
Anda **TIDAK PERLU** membuat tabel secara manual satu per satu, karena aplikasi ini telah ditanamkan logika untuk otomatis membuat database `koperasi_sekolah` dan seluruh tabel beserta kolom barunya saat pertama kali dijalankan!

Namun, jika karena suatu alasan sistem tidak bisa membuatnya otomatis, atau Anda ingin me- *reset* / mengimpor database secara manual, ikuti cara ini:
1. Buka browser dan akses **phpMyAdmin** di alamat: `http://localhost/phpmyadmin`
2. Pergi ke tab **SQL**.
3. Buka file `setup_db.sql` yang ada di dalam folder proyek ini menggunakan Notepad/Teks Editor.
4. *Copy* seluruh isinya, lalu *Paste* ke dalam kotak SQL di phpMyAdmin.
5. Klik **Kirim** (Go).
   
Seluruh struktur tabel (*users, siswa, barang, riwayat_restock, transaksi_tabungan, transaksi_toko, detail_transaksi_toko*) akan langsung terbuat dengan rapi dan terhubung satu sama lain.

## Login Pertama Kali (Akun Default)
Saat aplikasi pertama kali dijalankan (dan database masih kosong), program akan secara otomatis menyuntikkan satu akun Admin *default* ke dalam sistem agar Anda bisa *login*.
Gunakan akun ini untuk masuk ke aplikasi:
- **Username:** `admin`
- **Password:** `admin123`

## Pemecahan Masalah (Troubleshooting)
- **"Gagal terhubung ke MySQL!"** -> Pastikan XAMPP/Laragon Anda sudah aktif dan modul MySQL sedang berjalan (*Started*).
- **"Access denied for user 'root'@'localhost'"** -> Ini berarti MySQL lokal Anda dipasangi *password*. Silakan sesuaikan kredensial di `DBHelper.java`.
- **Error timezone / zona waktu** -> Jangan khawatir, ini sudah ditangani secara *built-in*. Koneksi JDBC di aplikasi ini sudah menggunakan parameter `serverTimezone=UTC` untuk mengatasinya secara otomatis.

Semoga aplikasinya berjalan dengan mulus!
