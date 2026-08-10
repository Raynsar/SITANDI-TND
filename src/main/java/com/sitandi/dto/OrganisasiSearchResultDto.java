package com.sitandi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object untuk hasil pencarian global struktur organisasi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisasiSearchResultDto {
    private Long id;
    private String nama;
    private String tipe; // PROVINSI, KABUPATEN, KOTA, OPD, BIDANG, PERSONAL
    private String path; // Menunjukkan hierarki lengkap, misal: "Provinsi Jawa Timur > Dinas Komunikasi dan Informatika > Bidang Aplikasi"
    
    // Khusus untuk hasil bertipe PERSONAL
    private Long bidangId;
    private String jabatan;
    private String nip;
}
