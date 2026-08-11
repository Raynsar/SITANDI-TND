# Tata Naskah Dinas (SITANDI) - Panduan AI Agent

## 1. Overview Project
Proyek ini adalah sistem tata naskah dinas berbasis web.
*(Catatan: Fitur peminjaman alat TU telah dihapus secara menyeluruh pada fase rebranding).*

## 2. Tech Stack & Aturan Versi
- **Backend**: Java 21, Spring Boot 3.5.16 (dikunci mutlak, JANGAN upgrade ke 3.6.x atau Spring Boot 4).
- **Database**: MySQL.
- **Frontend**: Thymeleaf, Bootstrap 5.3, AdminLTE 4.0.0 (via CDN), Bootstrap Icons. (JANGAN gunakan Tailwind CSS).
- **Security**: Spring Security + JWT, CSRF Token via Cookie/Header. Autentikasi terintegrasi dengan API TNDE eksternal (menggunakan User-Agent khusus).

## 3. Struktur Modul & Fitur
- **Dashboard**: Halaman utama ringkasan data.
- **Struktur Organisasi**: Menampilkan tree hierarki bidang, OPD, dll. Dilengkapi dengan backend-based *Tree Filter Global Search*.
- **Kelola Pengguna**: Menyetujui/menolak pendaftaran akun (Khusus ADMIN).
- **Surat Masuk**: Mengambil data surat dari integrasi TNDE eksternal via API Entry Point (`/api/v1/*`).
- **Ganti Password**: Fitur untuk mengubah kredensial.

## 4. Panduan & Aturan Koding Khusus
1. **Namespace Java EE**: Selalu gunakan jakarta.* (bukan javax.*) untuk Servlet, JPA, dan Validation.
2. **Context-Path Awareness**: Aplikasi di-deploy sebagai file WAR (misal dengan path /sitandi/). Semua link HTML **wajib** menggunakan sintaks Thymeleaf 	h:href="@{/...}" dan di JS menggunakan window.APP_CONTEXT_PATH. Jangan *hardcode* path yang diawali garis miring langsung tanpa penyesuaian context-path.
3. **Keamanan CSRF**: Untuk operasi non-GET (POST, PUT, DELETE, PATCH), API Call via Javascript etch() harus selalu menyertakan header X-XSRF-TOKEN yang nilainya diambil dari *cookie* XSRF-TOKEN.
4. **Layouting**: Gunakan struktur layout bawaan AdminLTE 4 (yaitu  pp-wrapper,  pp-header,  pp-sidebar,  pp-main). JANGAN menggunakan CSS *flexbox* kustom yang menabrak *CSS Grid* AdminLTE.
5. **Gaya Komentar**: JANGAN menggunakan komentar per-baris yang mendikte kode. Gunakan komentar per-blok (section) yang menjelaskan **tujuan** atau **keputusan desain/bisnis**.

## 5. Catatan untuk Developer Selanjutnya: Pola Komunikasi Data (Full JSON API)
Sejak fase audit dan migrasi terakhir, SITANDI Web telah mengadopsi pola **API-First dengan HTML Shell**. Pola ini WAJIB diikuti untuk setiap penambahan halaman/fitur baru:
- **Thymeleaf HANYA untuk Shell**: Controller MVC reguler (seperti `AdminController`) HANYA boleh mengembalikan nama template HTML. **Dilarang keras** mengirim data dinamis via `Model` (misal: `model.addAttribute(...)`) atau melakukan *server-side rendering* data tabel dengan `th:each`.
- **Pengambilan Data via JSON Fetch**: Semua data dinamis harus diambil dari *frontend* menggunakan JavaScript `fetch()` yang memanggil endpoint REST API (di bawah `/api/v1/`). Data dirender ke DOM menggunakan manipulasi elemen JS (seperti `document.createElement`).
- **Pemisahan Controller**: Pisahkan controller untuk merender HTML shell (contoh: `InboxController.java` di package `com.sitandi.controller`) dengan controller REST API yang mengembalikan JSON (contoh: `InboxApiController.java` di package `com.sitandi.controller.api`).
- **Autentikasi & Session**: Autentikasi tetap menggunakan pola JWT via HTTP-only cookie. Filter security `/api/**` dikonfigurasi sebagai **STATELESS**, namun token TNDE masih disimpan di HTTP Session reguler yang dibuat saat proses login (di web filter chain). REST Controller dapat mengambil `request.getSession(false)` untuk mengakses data session ini tanpa melanggar sifat stateless filter API.
- **Handling 401 Unauthorized**: API akan mengembalikan status HTTP 401 jika sesi telah berakhir (bukan *redirect* 302). Kode JavaScript di *frontend* bertanggung jawab menangkap status 401 dan melakukan pengalihan secara manual ke `window.location.href = APP_CTX + 'login'`.
