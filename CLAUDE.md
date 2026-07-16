# SIPENJAM Alat TU

Aplikasi manajemen peminjaman alat TU (Tata Usaha). Backend Spring Boot untuk sistem 
pengajuan, approval, dan tracking peminjaman alat/sarana.

## Stack
- Spring Boot 3.5.16, Java 17
- Packaging `war` → di-deploy ke Tomcat 10.1.x eksternal (bukan embedded)
- Spring Data JPA + MySQL
- Spring Security (form login, role-based)
- Thymeleaf (server-side rendering)
- Frontend: vanilla JS (fetch API) + Tailwind CDN

## Aturan Penting
- Import servlet WAJIB pakai namespace `jakarta.servlet.*` (HttpServletRequest, Filter, dll),
  JANGAN `javax.servlet.*` — Spring Boot 3.x + Tomcat 10 sudah pindah ke Jakarta EE 9+,
  `javax.servlet.*` tidak akan resolve. Berlaku juga untuk `jakarta.persistence.*` &
  `jakarta.validation.*`.
- Versi Spring Boot DIKUNCI di cabang 3.x (3.5.16) karena infra pakai Tomcat 10 dan
  Spring Boot 4.x butuh Servlet 6.1/Tomcat 11. Jangan bump ke 4.x. Detail & konsekuensi
  EOL ada di README section "Kebijakan Versi".
- URL harus context-path-aware (app jalan di `/sipenjam`, bukan root):
  Thymeleaf pakai `@{/path}` (jangan `href="/path"`), JS pakai `window.APP_CONTEXT_PATH`
  (jangan `window.location.origin`), Java pakai `request.getContextPath()` sebelum
  `sendRedirect()`. Halaman baru taruh di `templates/`, bukan `static/`.
- Frontend (`src/main/resources/static/`, `src/main/resources/templates/`) dan `pom.xml` 
  SUDAH FIX — jangan diubah kecuali diminta eksplisit.
- Semua REST endpoint di bawah `/api/v1/peminjaman` — cek `static/js/api.js` dan 
  `static/js/modules/*.js` untuk lihat kontrak yang sudah dipakai frontend, jangan ubah path-nya.
- Model attribute Thymeleaf harus sama persis dengan yang dipakai tiap file `.html` 
  (cek `th:text`/`th:each` di templates sebelum bikin controller).
- Password wajib di-hash BCrypt, jangan pernah plaintext.
- `/admin/**` dan endpoint modifikasi (PUT/PATCH inventaris, setujui/tolak peminjaman) 
  hanya untuk ROLE_ADMIN — cek ownership di service layer, bukan cuma di controller.
- User biasa hanya boleh akses/modifikasi peminjaman miliknya sendiri.
- Validasi semua input DTO pakai `@Valid`, tanggal kembali tidak boleh sebelum tanggal pinjam.
- `GlobalExceptionHandler` jangan bocorkan stack trace ke response JSON.

## Struktur Package
`com.sipenjam.peminjamanalattu` → `config/`, `controller/` (+ `controller/api/` untuk REST), 
`model/`, `repository/`, `service/` (+ `service/impl/`), `dto/`, `exception/`

## Commands
- `mvn spring-boot:run` — jalankan aplikasi lokal (embedded Tomcat, port 8080)
- `mvn compile` — cek build tanpa run
- `mvn test` — jalankan test
- `mvn clean package` — build WAR untuk deploy (`target/*.war`)

## Database
- `spring.jpa.hibernate.ddl-auto=update` saat development (auto-generate tabel dari entity).
  Ganti ke `validate` setelah schema stabil.