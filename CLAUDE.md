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
4. **Layouting**: Gunakan struktur layout bawaan AdminLTE 4 (yaitu pp-wrapper, pp-header, pp-sidebar, pp-main). JANGAN menggunakan CSS *flexbox* kustom yang menabrak *CSS Grid* AdminLTE.
5. **Gaya Komentar**: JANGAN menggunakan komentar per-baris yang mendikte kode. Gunakan komentar per-blok (section) yang menjelaskan **tujuan** atau **keputusan desain/bisnis**.
