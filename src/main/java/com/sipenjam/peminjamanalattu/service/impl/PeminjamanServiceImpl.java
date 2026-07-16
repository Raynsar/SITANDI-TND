package com.sipenjam.peminjamanalattu.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sipenjam.peminjamanalattu.dto.PeminjamanRequestDto;
import com.sipenjam.peminjamanalattu.dto.PeminjamanResponseDto;
import com.sipenjam.peminjamanalattu.dto.PengembalianRequestDto;
import com.sipenjam.peminjamanalattu.exception.ResourceNotFoundException;
import com.sipenjam.peminjamanalattu.model.Alat;
import com.sipenjam.peminjamanalattu.model.Peminjaman;
import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.Role;
import com.sipenjam.peminjamanalattu.model.StatusPeminjaman;
import com.sipenjam.peminjamanalattu.repository.AlatRepository;
import com.sipenjam.peminjamanalattu.repository.PeminjamanRepository;
import com.sipenjam.peminjamanalattu.service.AuthService;
import com.sipenjam.peminjamanalattu.service.PeminjamanService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PeminjamanServiceImpl implements PeminjamanService {

    /** Status yang dianggap "aktif" (alat sedang di tangan peminjam). */
    private static final List<StatusPeminjaman> STATUS_AKTIF =
            List.of(StatusPeminjaman.DISETUJUI, StatusPeminjaman.DIPINJAM);

    private final PeminjamanRepository peminjamanRepository;
    private final AlatRepository alatRepository;
    private final AuthService authService;

    // ============================ USER ============================

    @Override
    @Transactional
    public PeminjamanResponseDto ajukan(PeminjamanRequestDto dto, String username) {
        Pengguna pengguna = authService.getByUsername(username);
        Alat alat = alatRepository.findById(dto.getAlatId())
                .orElseThrow(() -> new ResourceNotFoundException("Alat", dto.getAlatId()));

        // Pertahanan berlapis: aturan tanggal juga dicek di DTO (@AssertTrue).
        if (dto.getTanggalKembali().isBefore(dto.getTanggalPinjam())) {
            throw new IllegalArgumentException("Tanggal kembali tidak boleh sebelum tanggal pinjam");
        }
        if (alat.getStok() <= 0) {
            throw new IllegalStateException("Stok alat '" + alat.getNama() + "' sedang tidak tersedia");
        }

        Peminjaman peminjaman = Peminjaman.builder()
                .alat(alat)
                .pengguna(pengguna)
                .tanggalPinjam(dto.getTanggalPinjam())
                .tanggalKembaliRencana(dto.getTanggalKembali())
                .keperluan(dto.getKeperluan())
                .status(StatusPeminjaman.MENUNGGU)
                .build();

        return PeminjamanResponseDto.fromEntity(peminjamanRepository.save(peminjaman));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeminjamanResponseDto> riwayat(String username) {
        Pengguna pengguna = authService.getByUsername(username);
        return peminjamanRepository.findByPenggunaOrderByIdDesc(pengguna).stream()
                .map(PeminjamanResponseDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public PeminjamanResponseDto batal(Long id, String username) {
        Peminjaman peminjaman = findOrThrow(id);
        pastikanPemilikAtauAdmin(peminjaman, username);

        if (peminjaman.getStatus() != StatusPeminjaman.MENUNGGU) {
            throw new IllegalStateException("Hanya peminjaman berstatus MENUNGGU yang bisa dibatalkan");
        }
        peminjaman.setStatus(StatusPeminjaman.DIBATALKAN);
        return PeminjamanResponseDto.fromEntity(peminjamanRepository.save(peminjaman));
    }

    @Override
    @Transactional
    public PeminjamanResponseDto kembalikan(Long id, PengembalianRequestDto dto, String username) {
        Peminjaman peminjaman = findOrThrow(id);
        pastikanPemilikAtauAdmin(peminjaman, username);

        if (!STATUS_AKTIF.contains(peminjaman.getStatus())) {
            throw new IllegalStateException("Alat hanya bisa dikembalikan jika peminjaman sedang aktif");
        }

        peminjaman.setStatus(StatusPeminjaman.DIKEMBALIKAN);
        peminjaman.setTanggalDikembalikan(LocalDate.now());
        peminjaman.setKondisiSaatKembali(dto.getKondisiSaatKembali());
        peminjaman.setCatatanPengembalian(dto.getCatatan());

        // Kembalikan stok alat.
        Alat alat = peminjaman.getAlat();
        alat.setStok(alat.getStok() + 1);
        alatRepository.save(alat);

        return PeminjamanResponseDto.fromEntity(peminjamanRepository.save(peminjaman));
    }

    @Override
    @Transactional(readOnly = true)
    public PeminjamanResponseDto statusPengembalian(Long id, String username) {
        Peminjaman peminjaman = findOrThrow(id);
        pastikanPemilikAtauAdmin(peminjaman, username);
        return PeminjamanResponseDto.fromEntity(peminjaman);
    }

    // ============================ ADMIN ============================

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PeminjamanResponseDto setujui(Long id) {
        Peminjaman peminjaman = findOrThrow(id);
        if (peminjaman.getStatus() != StatusPeminjaman.MENUNGGU) {
            throw new IllegalStateException("Hanya pengajuan MENUNGGU yang bisa disetujui");
        }

        Alat alat = peminjaman.getAlat();
        if (alat.getStok() <= 0) {
            throw new IllegalStateException("Stok alat '" + alat.getNama() + "' habis, tidak bisa disetujui");
        }
        alat.setStok(alat.getStok() - 1);
        alatRepository.save(alat);

        peminjaman.setStatus(StatusPeminjaman.DISETUJUI);
        return PeminjamanResponseDto.fromEntity(peminjamanRepository.save(peminjaman));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public PeminjamanResponseDto tolak(Long id) {
        Peminjaman peminjaman = findOrThrow(id);
        if (peminjaman.getStatus() != StatusPeminjaman.MENUNGGU) {
            throw new IllegalStateException("Hanya pengajuan MENUNGGU yang bisa ditolak");
        }
        peminjaman.setStatus(StatusPeminjaman.DITOLAK);
        return PeminjamanResponseDto.fromEntity(peminjamanRepository.save(peminjaman));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<PeminjamanResponseDto> daftarMenunggu() {
        return peminjamanRepository.findByStatusOrderByIdDesc(StatusPeminjaman.MENUNGGU).stream()
                .map(PeminjamanResponseDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<PeminjamanResponseDto> daftarPengembalian() {
        return peminjamanRepository.findByStatusOrderByIdDesc(StatusPeminjaman.DIKEMBALIKAN).stream()
                .map(PeminjamanResponseDto::fromEntity)
                .toList();
    }

    // ========================== DASHBOARD =========================

    @Override
    @Transactional(readOnly = true)
    public long jumlahAktif() {
        return peminjamanRepository.countByStatusIn(STATUS_AKTIF);
    }

    @Override
    @Transactional(readOnly = true)
    public long jumlahMenunggu() {
        return peminjamanRepository.countByStatus(StatusPeminjaman.MENUNGGU);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> logAktivitas() {
        return peminjamanRepository.findAllByOrderByIdDesc().stream()
                .limit(10)
                .map(p -> String.format("%s meminjam %s — status %s",
                        p.getPengguna() != null ? p.getPengguna().getNama() : "-",
                        p.getAlat() != null ? p.getAlat().getNama() : "-",
                        p.getStatus()))
                .toList();
    }

    // ========================== HELPERS ===========================

    private Peminjaman findOrThrow(Long id) {
        return peminjamanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Peminjaman", id));
    }

    /**
     * Cek ownership di service layer (bukan hanya controller). Admin boleh
     * mengakses semua; user biasa hanya peminjaman miliknya sendiri.
     */
    private void pastikanPemilikAtauAdmin(Peminjaman peminjaman, String username) {
        Pengguna pengguna = authService.getByUsername(username);
        boolean admin = pengguna.getRole() == Role.ADMIN;
        boolean pemilik = peminjaman.getPengguna() != null
                && peminjaman.getPengguna().getId().equals(pengguna.getId());
        if (!admin && !pemilik) {
            throw new AccessDeniedException("Anda tidak berhak mengakses peminjaman ini");
        }
    }
}
