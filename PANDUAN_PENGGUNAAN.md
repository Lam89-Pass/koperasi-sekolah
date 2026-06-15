# Panduan Penggunaan Aplikasi Koperasi Sekolah

Selamat datang di Sistem Informasi Koperasi Sekolah. Aplikasi ini dirancang untuk memudahkan segala transaksi harian di lingkungan koperasi, mulai dari kasir, pendataan barang, hingga simpan pinjam/tabungan siswa.

Berikut adalah panduan langkah demi langkah cara menggunakan aplikasi ini.

---

## 1. Login ke Aplikasi
Saat pertama kali membuka aplikasi, Anda akan dihadapkan pada layar Login.
- Masukkan **Username:** `admin`
- Masukkan **Password:** `admin123`
*(Ini adalah akun bawaan default, jika Anda belum mengubahnya).*

---

## 2. Mengenal Dashboard
Setelah berhasil masuk, Anda akan melihat halaman utama (Dashboard).
Di sini Anda dapat melihat:
- **Statistik Cepat:** Jumlah anggota siswa, total saldo tabungan, dan total omzet penjualan hari ini.
- **Aksi Cepat:** Tombol jalan pintas untuk langsung menuju Transaksi Baru, Setor Tunai, Tambah Barang, atau Daftar Siswa.
- **Aktivitas Terbaru:** Menampilkan log aktivitas transaksi yang baru saja terjadi di dalam sistem.

---

## 3. Manajemen Siswa (Menu "Siswa")
Gunakan menu ini untuk mendata anggota koperasi.
1. **Tambah Siswa:** 
   - Isi NIS, Nama Lengkap, dan Kelas pada form input sebelah kanan.
   - (Opsional) Isi Saldo Awal jika siswa langsung menyetor tabungan.
   - Klik **Simpan Data**.
2. **Edit Siswa:** Klik salah satu data siswa di tabel sebelah kiri, lalu ubah datanya di form kanan, dan klik **Simpan Data**.
3. **Pencarian:** Ketikkan nama atau NIS pada kotak pencarian di atas tabel untuk mencari siswa secara instan tanpa perlu scroll.
4. **Hapus/Nonaktifkan:** Jika siswa sudah lulus atau keluar, pilih datanya lalu klik **Nonaktifkan** atau **Hapus Permanen**.

---

## 4. Kelola Barang (Menu "Barang")
Gunakan menu ini untuk inventarisasi barang dagangan toko koperasi.
1. **Tambah Barang:**
   - Isi Kode Barang (contoh: BRG-001), Nama, Harga, Stok Awal, Kategori, dan Satuan.
   - Klik **Simpan Data**.
2. **Restock (Tambah Stok):** 
   - Klik tombol **(+)** warna hijau pada kolom *Aksi* di tabel barang.
   - Masukkan jumlah stok tambahan. Sistem akan otomatis menjumlahkannya dengan stok lama dan mencatat histori penambahan ke dalam database.
3. **Pencarian & Filter:** Anda dapat mencari barang berdasarkan kode/nama dan menyaringnya (*filter*) berdasarkan kategori barang.

---

## 5. Simpan & Tarik Tabungan (Menu "Tabungan")
Gunakan menu ini untuk memproses aktivitas simpan pinjam uang siswa.
1. **Cari Siswa:** Ketikkan NIS atau Nama siswa di kotak pencarian.
2. **Pilih Jenis Transaksi:** Klik tombol **Setor Tunai** (hijau) atau **Tarik Tunai** (kuning).
3. **Masukkan Nominal:** Ketik jumlah uang yang ingin disetor/ditarik.
4. **Keterangan:** (Opsional) misal: "Setoran wajib bulan Juni" atau "Tarik buat bayar LKS".
5. **Proses Transaksi:** Klik tombol proses di bawah. Sistem akan memvalidasi apakah saldo mencukupi (khusus untuk penarikan) dan otomatis memperbarui (*update*) total tabungan siswa.
6. **Cetak Struk:** Setelah transaksi sukses, klik "Cetak Struk" untuk menampilkan struk virtual format PDF/kertas.

---

## 6. Kasir / Toko Koperasi (Menu "Toko Koperasi")
Ini adalah fitur Point of Sales (POS) untuk melayani pembeli di kasir.
1. **Pilih Pembeli:** 
   - Jika pembelinya adalah anggota koperasi (siswa), pilih namanya dari *dropdown* "Pelanggan".
   - Jika pembelinya bukan anggota (Umum), biarkan saja pada mode "Umum".
2. **Pilih Barang:** 
   - Di daftar Katalog Produk (kiri), klik tombol **(+) Tambah ke Keranjang** pada barang yang ingin dibeli.
   - Barang akan otomatis masuk ke tabel **Keranjang Belanja** di sebelah kanan.
3. **Atur Keranjang:** Anda bisa menambah kuantitas jumlah barang, atau menekan tombol **Hapus Item** berwarna merah jika pembeli batal membeli barang tersebut.
4. **Pilih Metode Pembayaran:**
   - **CASH:** Pembayaran menggunakan uang tunai biasa.
   - **TABUNGAN:** Pembayaran dengan memotong saldo tabungan. *(PENTING: Metode ini HANYA bisa dipilih jika pelanggan yang dipilih di langkah 1 adalah Siswa terdaftar, dan saldo tabungannya mencukupi total belanjaan).*
5. **Checkout:** Klik tombol hijau **Checkout (Rp ...)**. Jika berhasil, otomatis stok barang di gudang akan berkurang.
6. **Cetak Struk Belanja:** Setelah sukses, tekan tombol "Cetak Struk" untuk mencetak resi kasir (format struk printer thermal).

---

## 7. Logout
Jika sudah selesai menggunakan aplikasi atau pergantian shift kasir, klik menu profil di pojok kanan atas, lalu pilih **Keluar dari Sistem** agar data tidak disalahgunakan orang lain.
