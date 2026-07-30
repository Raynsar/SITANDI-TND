package com.sipenjam.peminjamanalattu.service;

import java.util.List;

import com.sipenjam.peminjamanalattu.dto.OrganisasiTreeNodeDto;

public interface OrganisasiService {

    /**
     * Build seluruh tree struktur organisasi dari root (Provinsi) hingga level Jabatan.
     */
    List<OrganisasiTreeNodeDto> getTree();

    /**
     * Lazy-load children dari node Wilayah tertentu (opsional untuk tree besar).
     */
    OrganisasiTreeNodeDto getWilayahWithChildren(Long wilayahId);
}

