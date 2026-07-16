package com.sipenjam.peminjamanalattu.dto;

import com.sipenjam.peminjamanalattu.model.Kategori;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representasi ringkas kategori untuk dropdown form.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KategoriDto {

    private Long id;
    private String nama;

    public static KategoriDto fromEntity(Kategori k) {
        return new KategoriDto(k.getId(), k.getNama());
    }
}
