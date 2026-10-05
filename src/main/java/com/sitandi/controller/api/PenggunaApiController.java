package com.sitandi.controller.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sitandi.service.AuthService;
import com.sitandi.service.ExportService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint admin untuk data pengguna (mis. export). Base path /api/v1/pengguna.
 * Semua endpoint di sini wajib role ADMIN (diatur di SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/pengguna")
@RequiredArgsConstructor
public class PenggunaApiController {

    private final AuthService authService;
    private final ExportService exportService;

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] excel = exportService.penggunaKeExcel(authService.daftarSemuaPengguna());
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"daftar-pengguna.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] pdf = exportService.penggunaKePdf(authService.daftarSemuaPengguna(),
                "Daftar Pengguna SITANDI");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"daftar-pengguna.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
