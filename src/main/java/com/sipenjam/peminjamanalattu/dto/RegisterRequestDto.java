package com.sipenjam.peminjamanalattu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Body request pendaftaran akun mandiri (POST /api/v1/auth/register). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "Nama wajib diisi")
    @Size(max = 100, message = "Nama maksimal 100 karakter")
    private String nama;

    @NotBlank(message = "Username wajib diisi")
    @Size(min = 4, max = 30, message = "Username 4-30 karakter")
    // Dibatasi agar tidak bentrok dengan segmen URL & mudah dibaca admin.
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
             message = "Username hanya boleh huruf, angka, titik, garis bawah, dan strip")
    private String username;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    @Size(max = 100, message = "Email maksimal 100 karakter")
    private String email;

    @NotBlank(message = "Password wajib diisi")
    // Batas atas 72: BCrypt hanya memakai 72 byte pertama dan diam-diam mengabaikan
    // sisanya, sehingga password lebih panjang akan memberi rasa aman yang keliru.
    @Size(min = 8, max = 72, message = "Password minimal 8 karakter")
    private String password;

    @NotBlank(message = "Konfirmasi password wajib diisi")
    private String konfirmasiPassword;
}
