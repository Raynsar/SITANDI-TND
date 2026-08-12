package com.sitandi.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response gabungan untuk halaman Kelola Pengguna.
 * Mengembalikan daftar semua pengguna beserta jumlah yang menunggu aktivasi
 * dalam satu response JSON, sehingga frontend cukup satu kali fetch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KelolaPenggunaResponseDto {

    private List<PenggunaDto> daftarPengguna;
    private long jumlahMenungguAktivasi;
}
