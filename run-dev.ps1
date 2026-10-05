
# Pastikan JAVA_HOME menunjuk ke instalasi Java yang benar
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe" -ErrorAction SilentlyContinue)) {
    $javaPath = (Get-Command java -ErrorAction SilentlyContinue).Source
    if ($javaPath) {
        $env:JAVA_HOME = (Split-Path (Split-Path $javaPath))
        Write-Host " JAVA_HOME di-set otomatis ke: $env:JAVA_HOME" -ForegroundColor Yellow
    } else {
        Write-Host " ERROR: Java tidak ditemukan. Install JDK 17+ terlebih dahulu." -ForegroundColor Red
        exit 1
    }
}

# Generate random JWT_SECRET (48 random bytes → base64 encoded)
$env:JWT_SECRET = [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
Write-Host "================================================" -ForegroundColor Cyan
Write-Host " JWT_SECRET sudah di-generate untuk sesi ini." -ForegroundColor Green
Write-Host " (Secret baru dibuat setiap kali script dijalankan)" -ForegroundColor DarkGray
Write-Host "================================================" -ForegroundColor Cyan
# Mailtrap API Token
$env:MAILTRAP_API_TOKEN = "99509b148c25ba253251d7f0a752cf74"

if ($env:MAILTRAP_API_TOKEN -eq "<YOUR_API_TOKEN>" -or $env:MAILTRAP_API_TOKEN -eq "") {
    Write-Host " PERINGATAN: MAILTRAP_API_TOKEN masih kosong/default." -ForegroundColor Yellow
    Write-Host " Fitur Lupa Password akan gagal saat mengirim email." -ForegroundColor Yellow
}

# Jalankan Spring Boot
mvn spring-boot:run
