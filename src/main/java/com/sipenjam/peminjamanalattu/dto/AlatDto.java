package com.sipenjam.peminjamanalattu.dto;

import com.sipenjam.peminjamanalattu.model.Alat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representasi alat untuk katalog & inventaris.
 * Field {@code kategori} dan {@code kondisi} sengaja berupa String agar cocok
 * dengan {@code th:text="${alat.kategori}"} / {@code ${alat.kondisi}} di template.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlatDto {

    private Long id;
    private String nama;
    private String kategori;
    private int stok;
    private String kondisi;
    private String deskripsi;

    public static AlatDto fromEntity(Alat alat) {
        return AlatDto.builder()
                .id(alat.getId())
                .nama(alat.getNama())
                .kategori(alat.getKategori() != null ? alat.getKategori().getNama() : null)
                .stok(alat.getStok())
                .kondisi(alat.getKondisi() != null ? alat.getKondisi().name() : null)
                .deskripsi(alat.getDeskripsi())
                .build();
    }
}
