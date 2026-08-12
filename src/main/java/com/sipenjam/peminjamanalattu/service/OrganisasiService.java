package com.sipenjam.peminjamanalattu.service;

import java.util.List;

import com.sipenjam.peminjamanalattu.dto.OrganisasiBreadcrumbDto;
import com.sipenjam.peminjamanalattu.dto.OrganisasiPersonalDto;
import com.sipenjam.peminjamanalattu.dto.OrganisasiTreeNodeDto;

public interface OrganisasiService {

    /**
     * Build tree struktur organisasi dari root (Provinsi) → Kab/Kota → OPD → Bidang.
     * Tree TIDAK termasuk leaf Jabatan — jabatan di-load terpisah via {@link #getPersonal}.
     */
    List<OrganisasiTreeNodeDto> getTree();

    /**
     * Lazy-load children dari node Wilayah tertentu (opsional untuk tree besar).
     */
    OrganisasiTreeNodeDto getWilayahWithChildren(Long wilayahId);

    /**
     * Ambil daftar personal (nama + jabatan) untuk satu Bidang.
     * Dipakai untuk mengisi tabel kanan halaman tree saat user klik Bidang.
     */
    List<OrganisasiPersonalDto> getPersonal(Long bidangId);

    /**
     * Bangun breadcrumb (path dari root ke node) untuk node yang tipenya
     * diketahui. {@code tipe} salah satu dari {@code PROVINSI / KABUPATEN /
     * KOTA} (lihat {@link com.sipenjam.peminjamanalattu.model.TipeWilayah}),
     * atau {@code OPD} / {@code BIDANG}. Parameter tipe wajib disertakan
     * karena sequence id di tabel Wilayah / OPD / Bidang berdiri sendiri —
     * id yang sama bisa menunjuk ke node berbeda di tabel berbeda, dan
     * tanpa tipe server tidak bisa memastikan node mana yang diminta.
     *
     * @return DTO berisi list nama dari root ke node, dan string display
     *         'A › B › C'. Mengembalikan null bila node dengan id+tipe
     *         tersebut tidak ditemukan.
     */
    OrganisasiBreadcrumbDto getBreadcrumb(String tipe, Long id);
}

