# Sistem Tata Naskah Dinas (SITANDI)

SITANDI adalah aplikasi berbasis web yang digunakan untuk mengelola tata naskah dinas, manajemen struktur organisasi instansi, serta integrasi surat menyurat elektronik secara terpusat. Aplikasi ini dikembangkan menggunakan Spring Boot dan memiliki antarmuka (UI) berbasis AdminLTE 4.

## Fitur Utama
- **Autentikasi Terintegrasi**: Login terintegrasi dengan layanan TNDE eksternal (serta memiliki sistem fallback user lokal).
- **Struktur Organisasi**: Visualisasi dan pengelolaan hierarki wilayah, OPD, dan bidang/jabatan.
- **Kelola Pengguna**: Aktivasi akun pengguna oleh admin.
- **Surat Masuk**: Integrasi dengan inbox TNDE.
- **Dark Mode**: Dukungan mode gelap/terang bawaan AdminLTE 4.

## Prasyarat Lingkungan
- Java 21 (JDK 21)
- Apache Maven
- MySQL 8.x

## Cara Konfigurasi & Menjalankan (Development)

1. Clone repositori ini.
2. Buat database MySQL baru, sesuaikan konfigurasinya pada properti (contoh file pplication-local.properties).
3. Set environment variable JWT_SECRET dengan string acak (minimal 32 karakter) untuk keamanan token JWT.
   
   **Contoh di PowerShell (Windows):**
   `powershell
   $env:JWT_SECRET = "rahasia-super-panjang-untuk-sitandi-dev-minimal-32-karakter"
   `
4. Jalankan aplikasi menggunakan Maven:
   `ash
   mvn spring-boot:run
   `
5. Akses aplikasi melalui browser di: http://localhost:8080/

## Cara Build untuk Production (WAR)

Aplikasi ini dikonfigurasi untuk dibuild sebagai file WAR agar bisa di-deploy ke _standalone servlet container_ (misal: Apache Tomcat).

1. Jalankan build:
   `ash
   mvn clean package -DskipTests
   `
2. File WAR akan ter-generate di folder 	arget/tata-naskah-dinas-0.0.1-SNAPSHOT.war.
3. Deploy file WAR tersebut ke dalam folder webapps di Tomcat Anda. Perhatikan bahwa nama file akan menentukan _context-path_ URL aplikasi Anda (disarankan mengubah namanya menjadi sitandi.war agar URL menjadi /sitandi/).
