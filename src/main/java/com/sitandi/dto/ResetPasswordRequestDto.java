package com.sitandi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO untuk reset password — token + password baru.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequestDto {

    @NotBlank(message = "Token reset tidak boleh kosong")
    private String token;

    @NotBlank(message = "Password baru tidak boleh kosong")
    @Size(min = 8, max = 72, message = "Password baru harus 8-72 karakter")
    private String passwordBaru;

    @NotBlank(message = "Konfirmasi password tidak boleh kosong")
    private String konfirmasiPasswordBaru;
}
