# =============================================================================
# run-dev.ps1 — Helper script untuk menjalankan SITANDI secara lokal
# =============================================================================
# Script ini akan:
#   1. Men-generate JWT_SECRET secara acak (base64, 48 byte = 64 karakter)
#      dan menyimpannya sebagai environment variable untuk sesi ini saja.
#   2. Menjalankan aplikasi menggunakan Maven Spring Boot plugin.
#
# Cara pakai:
#   .\run-dev.ps1
# =============================================================================

# Generate random JWT_SECRET (48 random bytes → base64 encoded)
$env:JWT_SECRET = [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
Write-Host "================================================" -ForegroundColor Cyan
Write-Host " JWT_SECRET sudah di-generate untuk sesi ini." -ForegroundColor Green
Write-Host " (Secret baru dibuat setiap kali script dijalankan)" -ForegroundColor DarkGray
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Jalankan Spring Boot
mvn spring-boot:run
