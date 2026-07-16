package com.sipenjam.peminjamanalattu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sipenjam.peminjamanalattu.dto.AlatCreateDto;
import com.sipenjam.peminjamanalattu.dto.AlatDto;
import com.sipenjam.peminjamanalattu.dto.InventarisUpdateDto;
import com.sipenjam.peminjamanalattu.exception.ResourceNotFoundException;
import com.sipenjam.peminjamanalattu.model.Alat;
import com.sipenjam.peminjamanalattu.model.Kategori;
import com.sipenjam.peminjamanalattu.model.KondisiAlat;
import com.sipenjam.peminjamanalattu.repository.AlatRepository;
import com.sipenjam.peminjamanalattu.repository.KategoriRepository;
import com.sipenjam.peminjamanalattu.service.impl.AlatServiceImpl;

/**
 * Unit test layanan alat (tambah alat, update inventaris) tanpa database.
 */
@ExtendWith(MockitoExtension.class)
class AlatServiceImplTest {

    @Mock
    private AlatRepository alatRepository;
    @Mock
    private KategoriRepository kategoriRepository;

    @InjectMocks
    private AlatServiceImpl alatService;

    private AlatCreateDto createDto(Long kategoriId, String kategoriBaru) {
        AlatCreateDto dto = new AlatCreateDto();
        dto.setNama("Laptop Asus");
        dto.setKategoriId(kategoriId);
        dto.setKategoriBaru(kategoriBaru);
        dto.setStok(5);
        dto.setKondisi(KondisiAlat.BAIK);
        dto.setDeskripsi("Laptop kerja");
        return dto;
    }

    @Test
    @DisplayName("tambahAlat: kategori baru dibuat bila belum ada")
    void tambahAlat_kategoriBaru() {
        when(kategoriRepository.findByNama("Elektronik")).thenReturn(Optional.empty());
        when(kategoriRepository.save(any(Kategori.class)))
                .thenAnswer(inv -> {
                    Kategori k = inv.getArgument(0);
                    k.setId(9L);
                    return k;
                });
        when(alatRepository.save(any(Alat.class))).thenAnswer(inv -> inv.getArgument(0));

        AlatDto hasil = alatService.tambahAlat(createDto(null, "Elektronik"));

        assertThat(hasil.getNama()).isEqualTo("Laptop Asus");
        assertThat(hasil.getKategori()).isEqualTo("Elektronik");
        assertThat(hasil.getStok()).isEqualTo(5);
        verify(kategoriRepository).save(any(Kategori.class));
    }

    @Test
    @DisplayName("tambahAlat: memakai kategori yang sudah ada berdasarkan id")
    void tambahAlat_kategoriId() {
        Kategori multimedia = Kategori.builder().id(1L).nama("Multimedia").build();
        when(kategoriRepository.findById(1L)).thenReturn(Optional.of(multimedia));
        when(alatRepository.save(any(Alat.class))).thenAnswer(inv -> inv.getArgument(0));

        AlatDto hasil = alatService.tambahAlat(createDto(1L, null));

        assertThat(hasil.getKategori()).isEqualTo("Multimedia");
        verify(kategoriRepository, never()).save(any());
    }

    @Test
    @DisplayName("tambahAlat: gagal bila tidak memilih kategori maupun kategori baru")
    void tambahAlat_tanpaKategori() {
        assertThatThrownBy(() -> alatService.tambahAlat(createDto(null, "  ")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(alatRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateInventaris: memperbarui stok & kondisi")
    void updateInventaris_sukses() {
        Alat alat = Alat.builder().id(1L).nama("Proyektor").stok(3).kondisi(KondisiAlat.BAIK)
                .kategori(Kategori.builder().id(1L).nama("Multimedia").build()).build();
        when(alatRepository.findById(1L)).thenReturn(Optional.of(alat));
        when(alatRepository.save(any(Alat.class))).thenAnswer(inv -> inv.getArgument(0));

        InventarisUpdateDto dto = new InventarisUpdateDto();
        dto.setStok(10);
        dto.setKondisi(KondisiAlat.RUSAK_RINGAN);

        AlatDto hasil = alatService.updateInventaris(1L, dto);

        assertThat(hasil.getStok()).isEqualTo(10);
        assertThat(hasil.getKondisi()).isEqualTo("RUSAK_RINGAN");
    }

    @Test
    @DisplayName("updateInventaris: gagal bila alat tidak ditemukan")
    void updateInventaris_tidakAda() {
        when(alatRepository.findById(99L)).thenReturn(Optional.empty());

        InventarisUpdateDto dto = new InventarisUpdateDto();
        dto.setStok(1);
        dto.setKondisi(KondisiAlat.BAIK);

        assertThatThrownBy(() -> alatService.updateInventaris(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
