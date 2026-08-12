# SITANDI — Sistem Tata Naskah Dinas

Sistem informasi terpadu untuk pengelolaan naskah dinas, struktur organisasi, dan manajemen pengguna.

## Fitur Utama

- **Surat Masuk** — Integrasi dengan API TNDE (Tata Naskah Dinas Elektronik) untuk menampilkan inbox surat masuk
- **Struktur Organisasi** — Visualisasi tree OPD se-Jatim (Provinsi → Kab/Kota → OPD → Bidang → Personal)
- **Manajemen Pengguna** — Kelola akun, approval pendaftaran, aktivasi/nonaktifasi user
- **Dashboard Admin** — Ringkasan statistik sistem
- **Autentikasi** — Login lokal + integrasi login TNDE, role-based (ADMIN/USER)

## Stack

- Backend: Spring Boot 3.5.16 (Web, Thymeleaf, Spring Data JPA, Spring Security, Validation)
- Java 17
- Frontend: Thymeleaf + AdminLTE 4.0.0 (Bootstrap 5.3) + vanilla JS (fetch API)
- Database: MySQL (default, bisa diganti driver di `pom.xml`)
- Runtime produksi: Tomcat 10.1.x eksternal (WAR)

## Menjalankan Lokal (development)

```bash
# Set JWT_SECRET dulu (wajib, aplikasi menolak start bila kosong)
# Linux/Mac:
export JWT_SECRET=$(openssl rand -base64 48)
# Windows PowerShell:
$env:JWT_SECRET = [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))

mvn spring-boot:run
```

Akses di `http://localhost:8080`

Meski packaging-nya `war`, mode ini tetap jalan pakai embedded Tomcat karena `spring-boot-starter-tomcat` ber-scope `provided` (tetap ada di classpath saat `spring-boot:run` / `mvn test`, hanya dikecualikan dari isi WAR).

### Akun Default (DataSeeder)

| Username | Password  | Role       |
|----------|-----------|------------|
| admin    | admin123  | ROLE_ADMIN |
| user     | user123   | ROLE_USER  |

## Deployment ke Tomcat 10

Aplikasi di-package sebagai `.war` untuk di-deploy ke Tomcat 10 standalone milik Diskominfo Jatim, bukan pakai embedded Tomcat.

> **Prasyarat: Tomcat harus versi 10.1.x, bukan 10.0.x.**
>
> Spring Boot 3.x butuh Servlet 6.0 (Jakarta EE 10) — itu ada di Tomcat **10.1.x**.
> Tomcat **10.0.x** masih Servlet 5.0 (Jakarta EE 9) dan aplikasi ini TIDAK akan start di sana.
> Cek versi server dengan `$CATALINA_HOME/bin/version.sh`.
> Build ini di-test terhadap tomcat-embed 10.1.55 (bawaan Spring Boot 3.5.16).

### 1. Build

```bash
mvn clean package
```

Hasilnya: `target/sitandi.war`

### 2. Deploy

Copy WAR ke folder `webapps/` Tomcat. Nama file WAR menentukan context path:

```bash
cp target/sitandi.war $CATALINA_HOME/webapps/sitandi.war
```

Tomcat akan auto-deploy dan mengekstrak ke `webapps/sitandi/`.
Kasus khusus: `ROOT.war` → context path `/` (root).

### 3. Akses

```
http://<host>/<nama-war>/      contoh: http://10.10.10.5/sitandi/
```

Port ditentukan `<Connector port="80">` di `conf/server.xml` Tomcat — `server.port` di `application.properties` diabaikan saat mode WAR.

### 4. Konfigurasi environment (DB, dll)

`application.properties` ikut terbundel di dalam WAR. Untuk override tanpa rebuild, set via env var / system property di `bin/setenv.sh` Tomcat, contoh:

```bash
export JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.url=jdbc:mysql://dbhost:3306/sitandi \
                             -Dspring.datasource.username=sitandi \
                             -Dspring.datasource.password=**** \
                             -Dspring.jpa.hibernate.ddl-auto=validate"
```

### Catatan context path (penting)

Semua URL di frontend harus context-path-aware, karena app tidak lagi jalan di root:

- Template Thymeleaf: **selalu** pakai `@{/path}`, jangan `href="/path"` — `@{...}` otomatis menambahkan context path.
- JavaScript: pakai `window.APP_CONTEXT_PATH` (di-inject fragment `templates/common/context-path.html`, nilainya sudah termasuk trailing slash). Jangan pakai `window.location.origin` — origin tidak menyertakan context path. Fragment ini wajib di-include di `<head>` setiap halaman yang memuat JS.
- Java: `response.sendRedirect()` relatif ke root host, jadi prefix manual dengan `request.getContextPath()`.
- Halaman baru sebaiknya ditaruh di `templates/` (bukan `static/`) supaya bisa pakai `@{...}`; file di `static/` tidak diproses Thymeleaf.

## Kebijakan Versi (jangan diubah tanpa diskusi infra)

Project ini dikunci di **Spring Boot 3.5.16** — patch terakhir cabang 3.x, dan cabang 3.x sudah **EOL (end of OSS support) sejak Juni 2026**. Ini keputusan sadar, bukan kelalaian update:

- Runtime produksi Diskominfo Jatim adalah **Tomcat 10**, dan versi itu FIXED.
- Spring Boot 4.x butuh **Servlet 6.1**, yang hanya ada di **Tomcat 11**. Naik ke 4.x tanpa upgrade Tomcat akan bikin aplikasi gagal start.
- Jadi 3.5.16 dipilih sebagai versi 3.x paling baru di-patch selama constraint Tomcat 10 masih berlaku.

**Konsekuensi:** cabang 3.x tidak lagi menerima security patch gratis. Selama masih di 3.5.16, CVE baru di Spring/Tomcat tidak otomatis ter-cover — pantau manual, dan jadikan upgrade Tomcat 11 + Spring Boot 4.x sebagai rencana jangka menengah. Kalau kebijakan infra memungkinkan, opsi lain adalah langganan komersial (Tanzu Spring Enterprise) yang masih menyediakan patch untuk 3.5.x setelah EOL OSS.

**Namespace servlet:** Spring Boot 3.x + Tomcat 10 sudah pindah ke Jakarta EE 9+, jadi semua import servlet WAJIB `jakarta.servlet.*` — `javax.servlet.*` tidak akan resolve.

## Struktur Utama

- `controller/` — MVC controller untuk render halaman Thymeleaf
- `controller/api/` — REST controller untuk `/api/v1/organisasi/**` (dikonsumsi `static/js/api.js`)
- `model/` — entity JPA
- `repository/` — Spring Data JPA repository
- `service/` — interface + implementasi business logic
- `dto/` — request/response payload
- `exception/` — global exception handler

## Catatan Keamanan

- `SecurityConfig` mengatur role `ADMIN` vs `USER` (endpoint `/admin/**` hanya untuk admin).
- Password di-hash (BCrypt), tidak disimpan plaintext.
- CSRF diproteksi untuk endpoint state-changing. JWT untuk REST API divalidasi di setiap request.
- Validasi semua input DTO (`@Valid`) untuk cegah data korup/injection.
- Thymeleaf: gunakan `th:text` (auto-escape), hindari `th:utext` untuk data dari user (risiko XSS).