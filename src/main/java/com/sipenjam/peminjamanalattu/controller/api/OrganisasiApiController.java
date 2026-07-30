package com.sipenjam.peminjamanalattu.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sipenjam.peminjamanalattu.dto.OrganisasiTreeNodeDto;
import com.sipenjam.peminjamanalattu.service.OrganisasiService;

import lombok.RequiredArgsConstructor;

/**
 * REST endpoint untuk struktur organisasi (tree OPD se-Jatim).
 * Base path: /api/v1/organisasi (terpisah dari /api/v1/peminjaman)
 */
@RestController
@RequestMapping("/api/v1/organisasi")
@RequiredArgsConstructor
public class OrganisasiApiController {

    private final OrganisasiService organisasiService;

    /**
     * Mengembalikan seluruh tree struktur organisasi dari root (Provinsi)
     * hingga level Jabatan. Cocok untuk render awal halaman tree.
     */
    @GetMapping("/tree")
    public List<OrganisasiTreeNodeDto> getTree() {
        return organisasiService.getTree();
    }

    /**
     * Lazy-load: ambil satu node Wilayah beserta child langsungnya (kab/kota, OPD).
     * Berguna kalau tree terlalu besar untuk di-load sekaligus.
     */
    @GetMapping("/wilayah/{id}")
    public ResponseEntity<OrganisasiTreeNodeDto> getWilayah(@PathVariable Long id) {
        OrganisasiTreeNodeDto node = organisasiService.getWilayahWithChildren(id);
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node);
    }
}

