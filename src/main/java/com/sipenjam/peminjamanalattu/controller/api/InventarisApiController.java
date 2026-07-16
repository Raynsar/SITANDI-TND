package com.sipenjam.peminjamanalattu.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sipenjam.peminjamanalattu.dto.AlatCreateDto;
import com.sipenjam.peminjamanalattu.dto.AlatDto;
import com.sipenjam.peminjamanalattu.dto.InventarisUpdateDto;
import com.sipenjam.peminjamanalattu.service.AlatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoint katalog & inventaris alat.
 * Base path: /api/v1/peminjaman
 */
@RestController
@RequestMapping("/api/v1/peminjaman")
@RequiredArgsConstructor
public class InventarisApiController {

    private final AlatService alatService;

    @GetMapping("/inventaris")
    public List<AlatDto> daftarInventaris() {
        return alatService.getSemuaAlat();
    }

    /** Tambah alat baru. Otorisasi ADMIN dicek di service layer (@PreAuthorize). */
    @PostMapping("/inventaris")
    public ResponseEntity<AlatDto> tambahAlat(@Valid @RequestBody AlatCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alatService.tambahAlat(dto));
    }

    @GetMapping("/inventaris/{id}")
    public AlatDto detailInventaris(@PathVariable Long id) {
        return alatService.getDetail(id);
    }

    /** Update stok/kondisi alat. Otorisasi ADMIN dicek di service layer (@PreAuthorize). */
    @PutMapping("/inventaris/{id}")
    public AlatDto updateInventaris(@PathVariable Long id,
                                    @Valid @RequestBody InventarisUpdateDto dto) {
        return alatService.updateInventaris(id, dto);
    }
}
