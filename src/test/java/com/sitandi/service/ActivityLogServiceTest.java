package com.sitandi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sitandi.dto.ActivityLogDto;
import com.sitandi.model.ActivityLog;
import com.sitandi.model.Pengguna;
import com.sitandi.model.Role;
import com.sitandi.repository.ActivityLogRepository;

/**
 * Unit test Activity Log. Test Double: Mock ActivityLogRepository (verifikasi save dan
 * pemilihan query), Stub (hasil query), dan MockHttpServletRequest (Fake request).
 */
@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository repository;

    @InjectMocks
    private ActivityLogService service;

    @AfterEach
    void bersihkanRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private Pengguna admin() {
        return Pengguna.builder().id(1L).username("admin").nama("Admin").password("x")
                .role(Role.ADMIN).aktif(true).build();
    }

    private ActivityLog entri(Pengguna pengguna) {
        return ActivityLog.builder().id(1L).pengguna(pengguna).aksi("LOGIN")
                .deskripsi("Login berhasil").ipAddress("10.0.0.5")
                .createdAt(LocalDateTime.of(2026, 1, 15, 10, 0)).build();
    }

    private ActivityLog simpanDanAmbil() {
        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    // ================= log =================

    @Test
    @DisplayName("log tanpa request aktif: tersimpan dengan ip null")
    void log_tanpaRequest_ipNull() {
        service.log(admin(), "LOGIN", "Login berhasil");

        ActivityLog tersimpan = simpanDanAmbil();
        assertThat(tersimpan.getAksi()).isEqualTo("LOGIN");
        assertThat(tersimpan.getDeskripsi()).isEqualTo("Login berhasil");
        assertThat(tersimpan.getPengguna().getUsername()).isEqualTo("admin");
        assertThat(tersimpan.getIpAddress()).isNull();
        assertThat(tersimpan.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("log dengan request: ip diambil dari remoteAddr")
    void log_denganRequest_ipRemoteAddr() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.5");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        service.log(admin(), "LOGIN", "x");

        assertThat(simpanDanAmbil().getIpAddress()).isEqualTo("10.0.0.5");
    }

    @Test
    @DisplayName("log dengan header X-Forwarded-For: ip klien pertama dipakai")
    void log_denganXForwardedFor() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.5");
        req.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        service.log(admin(), "LOGIN", "x");

        assertThat(simpanDanAmbil().getIpAddress()).isEqualTo("203.0.113.7");
    }

    @Test
    @DisplayName("log dengan X-Forwarded-For kosong: kembali ke remoteAddr")
    void log_xForwardedForKosong_remoteAddr() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.9");
        req.addHeader("X-Forwarded-For", "  ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        service.log(null, "LOGIN_GAGAL", "user tidak dikenal");

        ActivityLog tersimpan = simpanDanAmbil();
        assertThat(tersimpan.getIpAddress()).isEqualTo("10.0.0.9");
        assertThat(tersimpan.getPengguna()).isNull();
    }

    // ================= cari =================

    @Test
    @DisplayName("cari: aksi + rentang tanggal -> query gabungan dengan batas awal/akhir hari")
    void cari_aksiDanTanggal() {
        LocalDate dari = LocalDate.of(2026, 1, 1);
        LocalDate sampai = LocalDate.of(2026, 1, 31);
        when(repository.findByAksiAndCreatedAtBetweenOrderByCreatedAtDesc("LOGIN",
                dari.atStartOfDay(), sampai.atTime(LocalTime.MAX)))
                .thenReturn(List.of(entri(admin())));

        List<ActivityLogDto> hasil = service.cari("LOGIN", dari, sampai);

        assertThat(hasil).hasSize(1);
        assertThat(hasil.get(0).getPengguna()).isEqualTo("admin");
        assertThat(hasil.get(0).getAksi()).isEqualTo("LOGIN");
    }

    @Test
    @DisplayName("cari: hanya aksi -> query berdasarkan aksi")
    void cari_hanyaAksi() {
        when(repository.findByAksiOrderByCreatedAtDesc("LOGIN")).thenReturn(List.of(entri(admin())));

        assertThat(service.cari("LOGIN", null, null)).hasSize(1);
    }

    @Test
    @DisplayName("cari: hanya rentang tanggal -> query berdasarkan tanggal")
    void cari_hanyaTanggal() {
        LocalDate dari = LocalDate.of(2026, 1, 1);
        LocalDate sampai = LocalDate.of(2026, 1, 31);
        when(repository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                dari.atStartOfDay(), sampai.atTime(LocalTime.MAX)))
                .thenReturn(List.of(entri(admin())));

        assertThat(service.cari(null, dari, sampai)).hasSize(1);
    }

    @Test
    @DisplayName("cari: tanpa filter -> semua log; pengguna null ditampilkan '-'")
    void cari_tanpaFilter_penggunaNullJadiStrip() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(entri(null)));

        List<ActivityLogDto> hasil = service.cari(null, null, null);

        assertThat(hasil.get(0).getPengguna()).isEqualTo("-");
    }

    @Test
    @DisplayName("cari: aksi kosong dan hanya tanggal awal -> jatuh ke semua log")
    void cari_aksiKosongTanggalSebagian_semuaLog() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<ActivityLogDto> hasil = service.cari("  ", LocalDate.of(2026, 1, 1), null);

        assertThat(hasil).isEmpty();
        verify(repository, never()).findByAksiOrderByCreatedAtDesc(any(String.class));
    }
}
