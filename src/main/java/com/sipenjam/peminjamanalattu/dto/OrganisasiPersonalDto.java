package com.sipenjam.peminjamanalattu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO untuk satu baris personal di panel kanan halaman struktur organisasi.
 * Dikembalikan oleh GET /api/v1/organisasi/bidang/{id}/personal.
 *
 * <p>Kolom tabel kanan: Nama | Jabatan | Username.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisasiPersonalDto {

    /** ID jabatan_struktur (untuk keying DOM row, bukan untuk display). */
    private Long id;

    /** Nama orang (dummy untuk presentasi). */
    private String nama;

    /** Jabatan: KETUA / SEKRETARIS / BENDAHARA / ANGGOTA. */
    private String jabatan;

    /**
     * Username dummy untuk tampilan (kolom Username di tabel). Bukan akun
     * login sungguhan, hanya string identitas visual hasil generate seeder
     * (inisial depan + nama belakang, lowercase, tanpa spasi).
     */
    private String username;
}
