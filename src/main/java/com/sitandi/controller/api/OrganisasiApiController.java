package com.sitandi.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import com.sitandi.dto.OrganisasiBreadcrumbDto;
import com.sitandi.dto.OrganisasiPersonalDto;
import com.sitandi.dto.OrganisasiSearchResultDto;
import com.sitandi.dto.OrganisasiTreeNodeDto;
import com.sitandi.dto.OrganisasiPersonalRequestDto;
import com.sitandi.service.OrganisasiService;

import lombok.RequiredArgsConstructor;

/**
 * REST endpoint untuk struktur organisasi (tree OPD se-Jatim).
 * Base path: /api/v1/organisasi (terpisah dari /api/v1/naskah)
 *
 * <p>GET /tree — seluruh tree (Provinsi → Kab/Kota → OPD → Bidang).
 * Tree TIDAK menyertakan leaf Jabatan, supaya payload tetap kecil untuk data
 * produksi nanti. Personal di-load terpisah lewat /bidang/{id}/personal saat
 * user memilih Bidang tertentu.</p>
 */
@RestController
@RequestMapping("/api/v1/organisasi")
@RequiredArgsConstructor
public class OrganisasiApiController {

    private final OrganisasiService organisasiService;

    /**
     * Mengembalikan seluruh tree struktur organisasi dari root (Provinsi)
     * sampai level Bidang. Tidak termasuk jabatan per orang.
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

/**
     * Ambil daftar personal (Nama + Jabatan + Username) untuk satu Bidang.
     * Dipanggil saat user mengklik node Bidang di panel kiri tree.
     * Mengembalikan array kosong (200) jika tidak ada data, bukan 404,
     * agar JS tidak perlu special-handle 404 untuk empty state.
     */
    @GetMapping("/bidang/{id}/personal")
    public ResponseEntity<List<OrganisasiPersonalDto>> getPersonal(@PathVariable Long id) {
        List<OrganisasiPersonalDto> personal = organisasiService.getPersonal(id);
        if (personal == null || personal.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(personal);
    }

    /**
     * Bangun breadcrumb (path dari root ke node) untuk sembarang node. FE
     * menyertakan parameter {@code tipe} supaya server tidak salah resolve
     * ketika id yang sama muncul di lebih dari satu tabel (Bidang / OPD /
     * Wilayah memakai sequence independent, sehingga tumpang tindih bisa
     * terjadi, mis. Bidang id=10 dan OPD id=10). Tipe yang valid:
     * {@code PROVINSI}, {@code KABUPATEN}, {@code KOTA}, {@code OPD},
     * {@code BIDANG}.
     */
    @GetMapping("/breadcrumb/{tipe}/{id}")
    public ResponseEntity<OrganisasiBreadcrumbDto> getBreadcrumb(@PathVariable String tipe,
                                                                 @PathVariable Long id) {
        OrganisasiBreadcrumbDto dto = organisasiService.getBreadcrumb(tipe, id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/bidang/{id}/personal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPersonal(@PathVariable Long id, @Valid @RequestBody OrganisasiPersonalRequestDto requestDto) {
        try {
            OrganisasiPersonalDto result = organisasiService.createPersonal(id, requestDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/personal/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePersonal(@PathVariable Long id, @Valid @RequestBody OrganisasiPersonalRequestDto requestDto) {
        try {
            OrganisasiPersonalDto result = organisasiService.updatePersonal(id, requestDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/personal/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePersonal(@PathVariable Long id) {
        organisasiService.deletePersonal(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrganisasiSearchResultDto>> searchOrganisasi(@org.springframework.web.bind.annotation.RequestParam("q") String keyword) {
        List<OrganisasiSearchResultDto> results = organisasiService.search(keyword);
        return ResponseEntity.ok(results);
    }
}

