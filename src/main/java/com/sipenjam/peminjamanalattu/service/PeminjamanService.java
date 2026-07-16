package com.sipenjam.peminjamanalattu.service;

import java.util.List;

import com.sipenjam.peminjamanalattu.dto.PeminjamanRequestDto;
import com.sipenjam.peminjamanalattu.dto.PeminjamanResponseDto;
import com.sipenjam.peminjamanalattu.dto.PengembalianRequestDto;

/**
 * Layanan siklus peminjaman: pengajuan, persetujuan, pembatalan, pengembalian.
 */
public interface PeminjamanService {

    // --- User ---
    PeminjamanResponseDto ajukan(PeminjamanRequestDto dto, String username);

    List<PeminjamanResponseDto> riwayat(String username);

    PeminjamanResponseDto batal(Long id, String username);

    PeminjamanResponseDto kembalikan(Long id, PengembalianRequestDto dto, String username);

    PeminjamanResponseDto statusPengembalian(Long id, String username);

    // --- Admin ---
    PeminjamanResponseDto setujui(Long id);

    PeminjamanResponseDto tolak(Long id);

    List<PeminjamanResponseDto> daftarMenunggu();

    /** Daftar peminjaman yang sudah dikembalikan (beserta kondisi & catatan). Admin. */
    List<PeminjamanResponseDto> daftarPengembalian();

    // --- Dashboard ---
    long jumlahAktif();

    long jumlahMenunggu();

    List<String> logAktivitas();
}
