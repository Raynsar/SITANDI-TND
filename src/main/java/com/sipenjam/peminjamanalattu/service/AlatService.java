package com.sipenjam.peminjamanalattu.service;

import java.util.List;

import com.sipenjam.peminjamanalattu.dto.AlatCreateDto;
import com.sipenjam.peminjamanalattu.dto.AlatDto;
import com.sipenjam.peminjamanalattu.dto.InventarisUpdateDto;
import com.sipenjam.peminjamanalattu.dto.KategoriDto;

/**
 * Layanan katalog & inventaris alat.
 */
public interface AlatService {

    /** Semua alat untuk katalog/inventaris. */
    List<AlatDto> getSemuaAlat();

    /** Detail satu alat. */
    AlatDto getDetail(Long id);

    /** Tambah alat baru. Hanya ADMIN. */
    AlatDto tambahAlat(AlatCreateDto dto);

    /** Update stok & kondisi alat. Hanya ADMIN. */
    AlatDto updateInventaris(Long id, InventarisUpdateDto dto);

    /** Daftar kategori (untuk dropdown form tambah alat). */
    List<KategoriDto> getSemuaKategori();

    /** Jumlah total jenis alat (untuk dashboard). */
    long totalAlat();
}
