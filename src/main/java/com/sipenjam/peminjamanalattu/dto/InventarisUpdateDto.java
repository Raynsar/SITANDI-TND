package com.sipenjam.peminjamanalattu.dto;

import com.sipenjam.peminjamanalattu.model.KondisiAlat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload update inventaris (ADMIN) dari Inventaris.js: { stok, kondisi }.
 */
@Getter
@Setter
public class InventarisUpdateDto {

    @NotNull(message = "Stok wajib diisi")
    @PositiveOrZero(message = "Stok tidak boleh negatif")
    private Integer stok;

    @NotNull(message = "Kondisi wajib diisi")
    private KondisiAlat kondisi;
}
