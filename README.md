# Sistem Tata Naskah Dinas (SITANDI)

SITANDI adalah aplikasi berbasis web yang digunakan untuk mengelola tata naskah dinas, manajemen struktur organisasi instansi, serta integrasi surat menyurat elektronik secara terpusat. Aplikasi ini dikembangkan menggunakan Spring Boot dan memiliki antarmuka (UI) berbasis AdminLTE 4.

## Fitur Utama
- **Autentikasi Terintegrasi**: Login terintegrasi dengan layanan TNDE eksternal (serta memiliki sistem fallback user lokal).
- **Struktur Organisasi (Tree Filter Search)**: Visualisasi dan pengelolaan hierarki wilayah, OPD, bidang, serta personil. Dilengkapi dengan fitur **Global Search** berbasis *tree filter* yang sangat responsif.
- **Kelola Pengguna**: Aktivasi akun pengguna oleh admin.
- **Surat Masuk**: Integrasi API Entry Point ke inbox TNDE.
- **API Entry Point Arsitektur**: Komunikasi terpusat melalui `/api/v1/*` sebagai pintu masuk frontend menuju backend.
- **Dark Mode**: Dukungan mode gelap/terang bawaan AdminLTE 4.

## Prasyarat Lingkungan
- Java 21 (JDK 21)
- Apache Maven
- MySQL 8.x

## Cara Konfigurasi & Menjalankan (Development)

1. Clone repositori ini.
2. Buat database MySQL baru, sesuaikan konfigurasinya pada properti (contoh file `application-local.properties`).

### ⚡ Cara Tercepat (Rekomendasi)

Gunakan script helper yang sudah disediakan. Script ini otomatis men-generate `JWT_SECRET` acak dan langsung menjalankan aplikasi:

```powershell
.\run-dev.ps1
```

> **Catatan:** `JWT_SECRET` di-generate ulang setiap kali script dijalankan. Ini aman untuk development karena token JWT hanya perlu valid selama sesi berjalan.

### Cara Manual

Jika ingin mengatur `JWT_SECRET` sendiri (misalnya agar token tetap valid antar-restart):

3. Set environment variable `JWT_SECRET` dengan string acak (minimal 32 karakter) untuk keamanan token JWT.
   
   **Contoh di PowerShell (Windows):**
   ```powershell
   $env:JWT_SECRET = "rahasia-super-panjang-untuk-sitandi-dev-minimal-32-karakter"
   ```
4. Jalankan aplikasi menggunakan Maven:
   ```bash
   mvn spring-boot:run
   ```

### Akses Aplikasi

5. Buka browser di: http://localhost:8080/

## Cara Build untuk Production (WAR)

Aplikasi ini dikonfigurasi untuk dibuild sebagai file WAR agar bisa di-deploy ke _standalone servlet container_ (misal: Apache Tomcat).

1. Jalankan build:
   ```bash
   mvn clean package -DskipTests
   ```
2. File WAR akan ter-generate di folder target/sitandi.war.
3. Deploy file WAR tersebut ke dalam folder webapps di Tomcat Anda. Perhatikan bahwa nama file akan menentukan _context-path_ URL aplikasi Anda (disarankan mengubah namanya menjadi sitandi.war agar URL menjadi /sitandi/).
