package com.sipenjam.peminjamanalattu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body response POST /api/v1/auth/login.
 *
 * Sengaja tidak memuat apa pun dari entity Pengguna selain username & role —
 * jangan pernah mengembalikan hash password ke klien.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    /** Access token, dikirim klien lewat header "Authorization: Bearer <token>". */
    private String token;

    private String username;

    /** Format Spring, mis. "ROLE_ADMIN". Informasi untuk klien; otorisasi tetap dicek server. */
    private String role;

    /** Masa berlaku token dalam milidetik, agar klien tahu kapan harus login ulang. */
    private long expiresInMs;
}
