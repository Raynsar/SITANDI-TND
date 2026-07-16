package com.sipenjam.peminjamanalattu.controller.api;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sipenjam.peminjamanalattu.dto.PeminjamanRequestDto;
import com.sipenjam.peminjamanalattu.dto.PeminjamanResponseDto;
import com.sipenjam.peminjamanalattu.service.PeminjamanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoint siklus peminjaman (pengajuan, riwayat, batal, persetujuan).
 * Base path: /api/v1/peminjaman
 */
@RestController
@RequestMapping("/api/v1/peminjaman")
@RequiredArgsConstructor
public class PeminjamanApiController {

    private final PeminjamanService peminjamanService;

    @PostMapping("/ajukan")
    public ResponseEntity<PeminjamanResponseDto> ajukan(@Valid @RequestBody PeminjamanRequestDto dto,
                                                        Principal principal) {
        PeminjamanResponseDto hasil = peminjamanService.ajukan(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(hasil);
    }

    @GetMapping("/riwayat")
    public List<PeminjamanResponseDto> riwayat(Principal principal) {
        return peminjamanService.riwayat(principal.getName());
    }

    @PatchMapping("/{id}/batal")
    public PeminjamanResponseDto batal(@PathVariable Long id, Principal principal) {
        return peminjamanService.batal(id, principal.getName());
    }

    // --- ADMIN ---

    @PatchMapping("/{id}/setujui")
    public PeminjamanResponseDto setujui(@PathVariable Long id) {
        return peminjamanService.setujui(id);
    }

    @PatchMapping("/{id}/tolak")
    public PeminjamanResponseDto tolak(@PathVariable Long id) {
        return peminjamanService.tolak(id);
    }
}
