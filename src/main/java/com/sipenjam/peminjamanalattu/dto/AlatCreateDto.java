package com.sipenjam.peminjamanalattu.dto;

import com.sipenjam.peminjamanalattu.model.KondisiAlat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload tambah alat baru (ADMIN).
 * Kategori bisa dipilih dari yang ada ({@code kategoriId}) atau diketik baru
 * ({@code kategoriBaru}); resolusi dilakukan di service layer.
 */
@Getter
@Setter
public class AlatCreateDto {

    @NotBlank(message = "Nama alat wajib diisi")
    private String nama;

    /** Id kategori dari dropdown (opsional bila mengisi kategoriBaru). */
    private Long kategoriId;

    /** Nama kategori baru (opsional bila memilih kategoriId). */
    private String kategoriBaru;

    @NotNull(message = "Stok wajib diisi")
    @PositiveOrZero(message = "Stok tidak boleh negatif")
    private Integer stok;

    @NotNull(message = "Kondisi wajib diisi")
    private KondisiAlat kondisi;

    private String deskripsi;
}
