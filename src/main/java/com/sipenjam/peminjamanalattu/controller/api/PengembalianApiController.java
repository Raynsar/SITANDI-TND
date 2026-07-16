package com.sipenjam.peminjamanalattu.controller.api;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sipenjam.peminjamanalattu.dto.PeminjamanResponseDto;
import com.sipenjam.peminjamanalattu.dto.PengembalianRequestDto;
import com.sipenjam.peminjamanalattu.service.PeminjamanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoint pengembalian alat.
 * Base path: /api/v1/peminjaman
 */
@RestController
@RequestMapping("/api/v1/peminjaman")
@RequiredArgsConstructor
public class PengembalianApiController {

    private final PeminjamanService peminjamanService;

    @PatchMapping("/{id}/kembalikan")
    public PeminjamanResponseDto kembalikan(@PathVariable Long id,
                                            @Valid @RequestBody PengembalianRequestDto dto,
                                            Principal principal) {
        return peminjamanService.kembalikan(id, dto, principal.getName());
    }

    @GetMapping("/{id}/status-pengembalian")
    public PeminjamanResponseDto statusPengembalian(@PathVariable Long id, Principal principal) {
        return peminjamanService.statusPengembalian(id, principal.getName());
    }
}
