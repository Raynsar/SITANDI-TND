
# Generate random JWT_SECRET (48 random bytes → base64 encoded)
$env:JWT_SECRET = [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
Write-Host "================================================" -ForegroundColor Cyan
Write-Host " JWT_SECRET sudah di-generate untuk sesi ini." -ForegroundColor Green
Write-Host " (Secret baru dibuat setiap kali script dijalankan)" -ForegroundColor DarkGray
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Jalankan Spring Boot
mvn spring-boot:run
