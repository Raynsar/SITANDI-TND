package com.sipenjam.peminjamanalattu.dto;

import com.sipenjam.peminjamanalattu.model.KondisiAlat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload konfirmasi pengembalian dari Pengembalian.js: { kondisiSaatKembali, catatan }.
 */
@Getter
@Setter
public class PengembalianRequestDto {

    @NotNull(message = "Kondisi saat kembali wajib diisi")
    private KondisiAlat kondisiSaatKembali;

    private String catatan;
}
