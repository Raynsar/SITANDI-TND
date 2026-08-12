package com.sitandi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO untuk request tambah/edit personel.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisasiPersonalRequestDto {

    @NotBlank(message = "Nama personel tidak boleh kosong")
    private String nama;

    @NotBlank(message = "Jabatan tidak boleh kosong")
    private String jabatan;

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;
}
