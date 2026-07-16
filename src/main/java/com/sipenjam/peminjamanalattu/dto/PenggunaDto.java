package com.sipenjam.peminjamanalattu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representasi akun untuk response API & halaman admin.
 *
 * Sengaja TIDAK memuat field password: entity Pengguna tidak boleh dikembalikan
 * langsung ke klien karena hash BCrypt-nya akan ikut terserialisasi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenggunaDto {

    private Long id;
    private String nama;
    private String username;
    private String email;
    private String role;
    private boolean aktif;
}
