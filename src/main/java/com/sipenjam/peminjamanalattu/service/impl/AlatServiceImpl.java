package com.sipenjam.peminjamanalattu.service.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sipenjam.peminjamanalattu.dto.AlatCreateDto;
import com.sipenjam.peminjamanalattu.dto.AlatDto;
import com.sipenjam.peminjamanalattu.dto.InventarisUpdateDto;
import com.sipenjam.peminjamanalattu.dto.KategoriDto;
import com.sipenjam.peminjamanalattu.exception.ResourceNotFoundException;
import com.sipenjam.peminjamanalattu.model.Alat;
import com.sipenjam.peminjamanalattu.model.Kategori;
import com.sipenjam.peminjamanalattu.repository.AlatRepository;
import com.sipenjam.peminjamanalattu.repository.KategoriRepository;
import com.sipenjam.peminjamanalattu.service.AlatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlatServiceImpl implements AlatService {

    private final AlatRepository alatRepository;
    private final KategoriRepository kategoriRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlatDto> getSemuaAlat() {
        return alatRepository.findAll().stream()
                .map(AlatDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlatDto getDetail(Long id) {
        return AlatDto.fromEntity(findAlatOrThrow(id));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public AlatDto tambahAlat(AlatCreateDto dto) {
        Kategori kategori = resolveKategori(dto);
        Alat alat = Alat.builder()
                .nama(dto.getNama())
                .kategori(kategori)
                .stok(dto.getStok())
                .kondisi(dto.getKondisi())
                .deskripsi(dto.getDeskripsi())
                .build();
        return AlatDto.fromEntity(alatRepository.save(alat));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public AlatDto updateInventaris(Long id, InventarisUpdateDto dto) {
        Alat alat = findAlatOrThrow(id);
        alat.setStok(dto.getStok());
        alat.setKondisi(dto.getKondisi());
        return AlatDto.fromEntity(alatRepository.save(alat));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KategoriDto> getSemuaKategori() {
        return kategoriRepository.findAll().stream()
                .map(KategoriDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long totalAlat() {
        return alatRepository.count();
    }

    private Alat findAlatOrThrow(Long id) {
        return alatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alat", id));
    }

    /**
     * Tentukan kategori dari kategoriBaru (dibuat bila belum ada) atau kategoriId.
     */
    private Kategori resolveKategori(AlatCreateDto dto) {
        if (dto.getKategoriBaru() != null && !dto.getKategoriBaru().isBlank()) {
            String nama = dto.getKategoriBaru().trim();
            return kategoriRepository.findByNama(nama)
                    .orElseGet(() -> kategoriRepository.save(Kategori.builder().nama(nama).build()));
        }
        if (dto.getKategoriId() != null) {
            return kategoriRepository.findById(dto.getKategoriId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategori", dto.getKategoriId()));
        }
        throw new IllegalArgumentException("Kategori wajib dipilih atau diisi kategori baru");
    }
}
