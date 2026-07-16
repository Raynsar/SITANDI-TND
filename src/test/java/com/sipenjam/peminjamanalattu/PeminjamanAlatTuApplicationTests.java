package com.sipenjam.peminjamanalattu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.sipenjam.peminjamanalattu.dto.PeminjamanRequestDto;
import com.sipenjam.peminjamanalattu.dto.PeminjamanResponseDto;
import com.sipenjam.peminjamanalattu.dto.PengembalianRequestDto;
import com.sipenjam.peminjamanalattu.exception.ResourceNotFoundException;
import com.sipenjam.peminjamanalattu.model.Alat;
import com.sipenjam.peminjamanalattu.model.Kategori;
import com.sipenjam.peminjamanalattu.model.KondisiAlat;
import com.sipenjam.peminjamanalattu.model.Peminjaman;
import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.Role;
import com.sipenjam.peminjamanalattu.model.StatusPeminjaman;
import com.sipenjam.peminjamanalattu.repository.AlatRepository;
import com.sipenjam.peminjamanalattu.repository.PeminjamanRepository;
import com.sipenjam.peminjamanalattu.service.AuthService;
import com.sipenjam.peminjamanalattu.service.impl.PeminjamanServiceImpl;

/**
 * Unit test logika bisnis peminjaman (tanpa Spring context / database).
 * Semua dependency di-mock dengan Mockito sehingga test cepat & tidak butuh MySQL.
 */
@ExtendWith(MockitoExtension.class)
class PeminjamanAlatTuApplicationTests {

    @Mock
    private PeminjamanRepository peminjamanRepository;
    @Mock
    private AlatRepository alatRepository;
    @Mock
    private AuthService authService;

    @InjectMocks
    private PeminjamanServiceImpl peminjamanService;

    private Pengguna user;
    private Pengguna adminUser;
    private Pengguna intruder;
    private Alat alat;

    @BeforeEach
    void setUp() {
        user = Pengguna.builder().id(1L).nama("Budi").username("user").role(Role.USER).build();
        adminUser = Pengguna.builder().id(99L).nama("Admin").username("admin").role(Role.ADMIN).build();
        intruder = Pengguna.builder().id(2L).nama("Orang Lain").username("lain").role(Role.USER).build();
        alat = Alat.builder()
                .id(1L).nama("Proyektor").stok(3).kondisi(KondisiAlat.BAIK)
                .kategori(Kategori.builder().id(1L).nama("Multimedia").build())
                .build();
    }

    private PeminjamanRequestDto requestDto(Long alatId, LocalDate pinjam, LocalDate kembali) {
        PeminjamanRequestDto dto = new PeminjamanRequestDto();
        dto.setAlatId(alatId);
        dto.setTanggalPinjam(pinjam);
        dto.setTanggalKembali(kembali);
        dto.setKeperluan("Rapat TU");
        return dto;
    }

    private Peminjaman peminjaman(StatusPeminjaman status, Pengguna pemilik) {
        return Peminjaman.builder()
                .id(10L).alat(alat).pengguna(pemilik)
                .tanggalPinjam(LocalDate.of(2026, 7, 15))
                .tanggalKembaliRencana(LocalDate.of(2026, 7, 18))
                .status(status).build();
    }

    // ============================ ajukan ============================

    @Test
    @DisplayName("ajukan: sukses membuat peminjaman berstatus MENUNGGU")
    void ajukan_sukses() {
        when(authService.getByUsername("user")).thenReturn(user);
        when(alatRepository.findById(1L)).thenReturn(Optional.of(alat));
        when(peminjamanRepository.save(any(Peminjaman.class))).thenAnswer(inv -> inv.getArgument(0));

        PeminjamanResponseDto hasil = peminjamanService.ajukan(
                requestDto(1L, LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 18)), "user");

        assertThat(hasil.getStatus()).isEqualTo("MENUNGGU");
        assertThat(hasil.getNamaAlat()).isEqualTo("Proyektor");
        assertThat(hasil.getNamaPemohon()).isEqualTo("Budi");
        verify(peminjamanRepository).save(any(Peminjaman.class));
    }

    @Test
    @DisplayName("ajukan: gagal bila stok alat habis")
    void ajukan_stokHabis() {
        alat.setStok(0);
        when(authService.getByUsername("user")).thenReturn(user);
        when(alatRepository.findById(1L)).thenReturn(Optional.of(alat));

        assertThatThrownBy(() -> peminjamanService.ajukan(
                requestDto(1L, LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 18)), "user"))
                .isInstanceOf(IllegalStateException.class);
        verify(peminjamanRepository, never()).save(any());
    }

    @Test
    @DisplayName("ajukan: gagal bila tanggal kembali sebelum tanggal pinjam")
    void ajukan_tanggalTerbalik() {
        when(authService.getByUsername("user")).thenReturn(user);
        when(alatRepository.findById(1L)).thenReturn(Optional.of(alat));

        assertThatThrownBy(() -> peminjamanService.ajukan(
                requestDto(1L, LocalDate.of(2026, 7, 18), LocalDate.of(2026, 7, 15)), "user"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(peminjamanRepository, never()).save(any());
    }

    @Test
    @DisplayName("ajukan: gagal bila alat tidak ditemukan")
    void ajukan_alatTidakAda() {
        when(authService.getByUsername("user")).thenReturn(user);
        when(alatRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> peminjamanService.ajukan(
                requestDto(1L, LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 18)), "user"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============================ batal ============================

    @Test
    @DisplayName("batal: sukses saat status MENUNGGU & dilakukan pemilik")
    void batal_sukses() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.MENUNGGU, user)));
        when(authService.getByUsername("user")).thenReturn(user);
        when(peminjamanRepository.save(any(Peminjaman.class))).thenAnswer(inv -> inv.getArgument(0));

        PeminjamanResponseDto hasil = peminjamanService.batal(10L, "user");

        assertThat(hasil.getStatus()).isEqualTo("DIBATALKAN");
    }

    @Test
    @DisplayName("batal: gagal bila status bukan MENUNGGU")
    void batal_statusSalah() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.DISETUJUI, user)));
        when(authService.getByUsername("user")).thenReturn(user);

        assertThatThrownBy(() -> peminjamanService.batal(10L, "user"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("batal: gagal (AccessDenied) bila bukan milik pengguna & bukan admin")
    void batal_bukanPemilik() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.MENUNGGU, user)));
        when(authService.getByUsername("lain")).thenReturn(intruder);

        assertThatThrownBy(() -> peminjamanService.batal(10L, "lain"))
                .isInstanceOf(AccessDeniedException.class);
        verify(peminjamanRepository, never()).save(any());
    }

    // ============================ setujui / tolak ============================

    @Test
    @DisplayName("setujui: MENUNGGU -> DISETUJUI dan stok berkurang 1")
    void setujui_sukses() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.MENUNGGU, user)));
        when(peminjamanRepository.save(any(Peminjaman.class))).thenAnswer(inv -> inv.getArgument(0));

        PeminjamanResponseDto hasil = peminjamanService.setujui(10L);

        assertThat(hasil.getStatus()).isEqualTo("DISETUJUI");
        assertThat(alat.getStok()).isEqualTo(2);
        verify(alatRepository).save(alat);
    }

    @Test
    @DisplayName("setujui: gagal bila status bukan MENUNGGU")
    void setujui_statusSalah() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.DISETUJUI, user)));

        assertThatThrownBy(() -> peminjamanService.setujui(10L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("setujui: gagal bila stok habis")
    void setujui_stokHabis() {
        alat.setStok(0);
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.MENUNGGU, user)));

        assertThatThrownBy(() -> peminjamanService.setujui(10L))
                .isInstanceOf(IllegalStateException.class);
        verify(alatRepository, never()).save(any());
    }

    @Test
    @DisplayName("tolak: MENUNGGU -> DITOLAK tanpa mengubah stok")
    void tolak_sukses() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.MENUNGGU, user)));
        when(peminjamanRepository.save(any(Peminjaman.class))).thenAnswer(inv -> inv.getArgument(0));

        PeminjamanResponseDto hasil = peminjamanService.tolak(10L);

        assertThat(hasil.getStatus()).isEqualTo("DITOLAK");
        assertThat(alat.getStok()).isEqualTo(3);
        verify(alatRepository, never()).save(any());
    }

    // ============================ kembalikan ============================

    @Test
    @DisplayName("kembalikan: DISETUJUI -> DIKEMBALIKAN, isi kondisi, stok bertambah 1")
    void kembalikan_sukses() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.DISETUJUI, user)));
        when(authService.getByUsername("user")).thenReturn(user);
        when(peminjamanRepository.save(any(Peminjaman.class))).thenAnswer(inv -> inv.getArgument(0));

        PengembalianRequestDto dto = new PengembalianRequestDto();
        dto.setKondisiSaatKembali(KondisiAlat.RUSAK_RINGAN);
        dto.setCatatan("lensa tergores");

        PeminjamanResponseDto hasil = peminjamanService.kembalikan(10L, dto, "user");

        assertThat(hasil.getStatus()).isEqualTo("DIKEMBALIKAN");
        assertThat(hasil.getKondisiSaatKembali()).isEqualTo("RUSAK_RINGAN");
        assertThat(hasil.getCatatanPengembalian()).isEqualTo("lensa tergores");
        assertThat(hasil.getTanggalDikembalikan()).isNotNull();
        assertThat(alat.getStok()).isEqualTo(4);
    }

    @Test
    @DisplayName("kembalikan: gagal bila peminjaman tidak sedang aktif")
    void kembalikan_belumAktif() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.MENUNGGU, user)));
        when(authService.getByUsername("user")).thenReturn(user);

        PengembalianRequestDto dto = new PengembalianRequestDto();
        dto.setKondisiSaatKembali(KondisiAlat.BAIK);

        assertThatThrownBy(() -> peminjamanService.kembalikan(10L, dto, "user"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("kembalikan: admin boleh memproses peminjaman milik pengguna lain")
    void kembalikan_adminBypassOwnership() {
        when(peminjamanRepository.findById(10L)).thenReturn(Optional.of(peminjaman(StatusPeminjaman.DISETUJUI, user)));
        when(authService.getByUsername("admin")).thenReturn(adminUser);
        when(peminjamanRepository.save(any(Peminjaman.class))).thenAnswer(inv -> inv.getArgument(0));

        PengembalianRequestDto dto = new PengembalianRequestDto();
        dto.setKondisiSaatKembali(KondisiAlat.BAIK);

        PeminjamanResponseDto hasil = peminjamanService.kembalikan(10L, dto, "admin");

        assertThat(hasil.getStatus()).isEqualTo("DIKEMBALIKAN");
    }

    // ============================ query ============================

    @Test
    @DisplayName("riwayat: mengembalikan hanya peminjaman milik pengguna")
    void riwayat_milikSendiri() {
        when(authService.getByUsername("user")).thenReturn(user);
        when(peminjamanRepository.findByPenggunaOrderByIdDesc(user))
                .thenReturn(List.of(peminjaman(StatusPeminjaman.MENUNGGU, user)));

        List<PeminjamanResponseDto> hasil = peminjamanService.riwayat("user");

        assertThat(hasil).hasSize(1);
        assertThat(hasil.get(0).getNamaPemohon()).isEqualTo("Budi");
    }

    @Test
    @DisplayName("daftarPengembalian: mengambil peminjaman berstatus DIKEMBALIKAN")
    void daftarPengembalian() {
        Peminjaman dikembalikan = peminjaman(StatusPeminjaman.DIKEMBALIKAN, user);
        dikembalikan.setKondisiSaatKembali(KondisiAlat.BAIK);
        when(peminjamanRepository.findByStatusOrderByIdDesc(StatusPeminjaman.DIKEMBALIKAN))
                .thenReturn(List.of(dikembalikan));

        List<PeminjamanResponseDto> hasil = peminjamanService.daftarPengembalian();

        assertThat(hasil).hasSize(1);
        assertThat(hasil.get(0).getKondisiSaatKembali()).isEqualTo("BAIK");
    }
}
