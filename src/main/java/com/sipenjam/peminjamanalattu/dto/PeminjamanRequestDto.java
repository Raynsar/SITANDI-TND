package com.sipenjam.peminjamanalattu.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload pengajuan peminjaman dari form-pinjam.html.
 * Nama field mengikuti kontrak frontend: { alatId, tanggalPinjam, tanggalKembali, keperluan }.
 */
@Getter
@Setter
public class PeminjamanRequestDto {

    @NotNull(message = "alatId wajib diisi")
    private Long alatId;

    @NotNull(message = "Tanggal pinjam wajib diisi")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalPinjam;

    @NotNull(message = "Tanggal kembali wajib diisi")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggalKembali;

    @NotBlank(message = "Keperluan wajib diisi")
    private String keperluan;

    /**
     * Aturan bisnis: tanggal kembali tidak boleh sebelum tanggal pinjam.
     * Divalidasi otomatis oleh @Valid.
     */
    @AssertTrue(message = "Tanggal kembali tidak boleh sebelum tanggal pinjam")
    public boolean isRentangTanggalValid() {
        if (tanggalPinjam == null || tanggalKembali == null) {
            return true; // biar pesan @NotNull yang menangani field kosong
        }
        return !tanggalKembali.isBefore(tanggalPinjam);
    }
}
