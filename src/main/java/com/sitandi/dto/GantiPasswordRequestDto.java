package com.sitandi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Body request ganti password milik sendiri (POST /api/v1/auth/ganti-password). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GantiPasswordRequestDto {

    /**
     * Password lama wajib diverifikasi ulang meski pengguna sudah login. Tanpa ini,
     * penyerang yang berhasil membajak sesi/token bisa mengunci pemilik akun dengan
     * mengganti passwordnya.
     */
    @NotBlank(message = "Password lama wajib diisi")
    private String passwordLama;

    @NotBlank(message = "Password baru wajib diisi")
    @Size(min = 8, max = 72, message = "Password baru minimal 8 karakter")
    private String passwordBaru;

    @NotBlank(message = "Konfirmasi password baru wajib diisi")
    private String konfirmasiPasswordBaru;
}
