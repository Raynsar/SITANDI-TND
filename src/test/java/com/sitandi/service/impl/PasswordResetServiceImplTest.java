package com.sitandi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.sitandi.model.PasswordResetToken;
import com.sitandi.model.Pengguna;
import com.sitandi.repository.PasswordResetTokenRepository;
import com.sitandi.repository.PenggunaRepository;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.MailtrapMail;

/**
 * Unit test fitur Reset Password (PasswordResetServiceImpl).
 *
 * Test Double yang dipakai:
 * - Stub  : PenggunaRepository dan PasswordResetTokenRepository (nilai kembalian disiapkan).
 * - Mock  : MailtrapClient dan PasswordResetTokenRepository (interaksi diverifikasi).
 * - Fake  : FakePasswordEncoder (implementasi sederhana pengganti BCrypt).
 * - Spy   : spy(FakePasswordEncoder) untuk memastikan encode() benar-benar dipanggil.
 *
 * Test Case ID mengikuti analisis Pre-Lab (TC-WB-01 s.d. TC-WB-16).
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private PenggunaRepository penggunaRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private MailtrapClient mailtrapClient;

    private PasswordEncoder passwordEncoder;
    private PasswordResetServiceImpl service;

    /** Fake: implementasi PasswordEncoder ringan, tanpa BCrypt. */
    static class FakePasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return "ENC:" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    }

    @BeforeEach
    void setUp() {
        passwordEncoder = spy(new FakePasswordEncoder());
        // Token Mailtrap kosong -> mailtrapClient null (jalur TC-WB-02 / TC-WB-07)
        service = new PasswordResetServiceImpl(penggunaRepository, tokenRepository, passwordEncoder, "", BASE_URL);
    }

    private void aktifkanMailtrap() {
        ReflectionTestUtils.setField(service, "mailtrapClient", mailtrapClient);
    }

    private Pengguna penggunaLokal() {
        return Pengguna.builder()
                .id(1L)
                .nama("Adifa")
                .username("adifa")
                .password("ENC:passwordLama1")
                .email("adifa@qa.local")
                .build();
    }

    private PasswordResetToken token(Pengguna pengguna, LocalDateTime expiredAt, boolean used) {
        return PasswordResetToken.builder()
                .token("token-uji")
                .pengguna(pengguna)
                .expiredAt(expiredAt)
                .used(used)
                .build();
    }

    // ================= Constructor =================

    @Test
    @DisplayName("TC-WB-01: token Mailtrap terisi -> MailtrapClient dibuat")
    void constructor_tokenTerisi_clientDibuat() {
        // Act
        PasswordResetServiceImpl svc = new PasswordResetServiceImpl(
                penggunaRepository, tokenRepository, passwordEncoder, "token-uji", BASE_URL);

        // Assert
        assertThat(ReflectionTestUtils.getField(svc, "mailtrapClient")).isNotNull();
    }

    @Test
    @DisplayName("TC-WB-02: token Mailtrap kosong -> client null, aplikasi tetap bisa dibuat")
    void constructor_tokenKosong_clientNull() {
        // Act
        PasswordResetServiceImpl svc = new PasswordResetServiceImpl(
                penggunaRepository, tokenRepository, passwordEncoder, "", BASE_URL);

        // Assert
        assertThat(ReflectionTestUtils.getField(svc, "mailtrapClient")).isNull();
    }

    // ================= requestReset =================

    @Test
    @DisplayName("TC-WB-03: requestReset dengan email hanya spasi ditolak")
    void requestReset_emailKosong_melemparIllegalArgument() {
        assertThatThrownBy(() -> service.requestReset("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email tidak boleh kosong");

        verifyNoInteractions(penggunaRepository, tokenRepository);
    }

    @Test
    @DisplayName("TC-WB-04: requestReset dengan email tidak terdaftar ditolak")
    void requestReset_emailTidakTerdaftar_melemparIllegalArgument() {
        // Arrange (Stub)
        when(penggunaRepository.findByEmail("tidakada@qa.local")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.requestReset("tidakada@qa.local"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email tidak ditemukan atau bukan akun lokal");

        verifyNoInteractions(tokenRepository);
    }

    @Test
    @DisplayName("TC-WB-05: akun tanpa email (guard defense-in-depth) ditolak")
    void requestReset_akunTanpaEmail_melemparIllegalArgument() {
        // Arrange (Stub): kondisi ini tidak bisa muncul dari data nyata, hanya lewat test double
        Pengguna tanpaEmail = penggunaLokal();
        tanpaEmail.setEmail(null);
        when(penggunaRepository.findByEmail("akun@qa.local")).thenReturn(Optional.of(tanpaEmail));

        // Act + Assert
        assertThatThrownBy(() -> service.requestReset("akun@qa.local"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email tidak ditemukan atau bukan akun lokal");

        verifyNoInteractions(tokenRepository);
    }

    @Test
    @DisplayName("TC-WB-06: requestReset sukses -> token lama dihapus, token baru 15 menit, email terkirim")
    void requestReset_sukses_tokenDisimpanDanEmailTerkirim() {
        // Arrange
        aktifkanMailtrap();
        Pengguna pengguna = penggunaLokal();
        when(penggunaRepository.findByEmail("adifa@qa.local")).thenReturn(Optional.of(pengguna));
        LocalDateTime sebelum = LocalDateTime.now();

        // Act (email diberi spasi untuk memastikan di-trim)
        service.requestReset("  adifa@qa.local  ");

        // Assert (Mock: urutan dan isi interaksi)
        InOrder urutan = inOrder(tokenRepository);
        urutan.verify(tokenRepository).deleteByPengguna(pengguna);
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        urutan.verify(tokenRepository).save(captor.capture());

        PasswordResetToken tersimpan = captor.getValue();
        assertThat(tersimpan.getToken()).hasSize(64);
        assertThat(tersimpan.getPengguna()).isSameAs(pengguna);
        assertThat(tersimpan.isUsed()).isFalse();
        assertThat(tersimpan.getExpiredAt())
                .isAfterOrEqualTo(sebelum.plusMinutes(15))
                .isBeforeOrEqualTo(LocalDateTime.now().plusMinutes(15));

        verify(mailtrapClient).send(any(MailtrapMail.class));
    }

    @Test
    @DisplayName("TC-WB-07: konfigurasi Mailtrap belum diatur -> IllegalStateException")
    void requestReset_mailtrapBelumDiatur_melemparIllegalState() {
        // Arrange: service dari setUp() memakai token kosong, jadi mailtrapClient = null
        when(penggunaRepository.findByEmail("adifa@qa.local")).thenReturn(Optional.of(penggunaLokal()));

        // Act + Assert
        assertThatThrownBy(() -> service.requestReset("adifa@qa.local"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Konfigurasi Mailtrap belum diatur");
    }

    @Test
    @DisplayName("TC-WB-08: pengiriman email melempar exception -> IllegalStateException informatif")
    void requestReset_pengirimanGagal_melemparIllegalState() {
        // Arrange (Mock yang dipaksa gagal)
        aktifkanMailtrap();
        when(penggunaRepository.findByEmail("adifa@qa.local")).thenReturn(Optional.of(penggunaLokal()));
        when(mailtrapClient.send(any(MailtrapMail.class))).thenThrow(new RuntimeException("koneksi putus"));

        // Act + Assert
        assertThatThrownBy(() -> service.requestReset("adifa@qa.local"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pastikan konfigurasi Mailtrap benar");
    }

    // ================= validateToken =================

    @Test
    @DisplayName("TC-WB-09: validateToken dengan token valid mengembalikan email pemilik")
    void validateToken_tokenValid_mengembalikanEmail() {
        // Arrange (Stub)
        when(tokenRepository.findByToken("token-uji"))
                .thenReturn(Optional.of(token(penggunaLokal(), LocalDateTime.now().plusMinutes(10), false)));

        // Act
        String email = service.validateToken("token-uji");

        // Assert
        assertThat(email).isEqualTo("adifa@qa.local");
    }

    // ================= resetPassword =================

    @Test
    @DisplayName("TC-WB-10: resetPassword dengan password baru kosong ditolak")
    void resetPassword_passwordKosong_melemparIllegalArgument() {
        assertThatThrownBy(() -> service.resetPassword("token-uji", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password baru tidak boleh kosong");

        verifyNoInteractions(tokenRepository);
    }

    @Test
    @DisplayName("TC-WB-11: resetPassword dengan password 7 karakter (di bawah batas) ditolak")
    void resetPassword_passwordTujuhKarakter_melemparIllegalArgument() {
        assertThatThrownBy(() -> service.resetPassword("token-uji", "Abc1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password baru minimal 8 karakter");

        verifyNoInteractions(tokenRepository);
    }

    @Test
    @DisplayName("TC-WB-12: resetPassword dengan token kosong ditolak")
    void resetPassword_tokenKosong_melemparIllegalArgument() {
        assertThatThrownBy(() -> service.resetPassword(" ", "PasswordBaru1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token reset tidak boleh kosong");

        verifyNoInteractions(tokenRepository);
    }

    @Test
    @DisplayName("TC-WB-13: resetPassword dengan token tidak dikenal ditolak")
    void resetPassword_tokenTidakDitemukan_melemparIllegalArgument() {
        // Arrange (Stub)
        when(tokenRepository.findByToken("tokenacak123")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.resetPassword("tokenacak123", "PasswordBaru1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token reset tidak valid");
    }

    @Test
    @DisplayName("TC-WB-14: resetPassword dengan token yang sudah dipakai ditolak")
    void resetPassword_tokenSudahDipakai_melemparIllegalArgument() {
        // Arrange (Stub)
        when(tokenRepository.findByToken("token-uji"))
                .thenReturn(Optional.of(token(penggunaLokal(), LocalDateTime.now().plusMinutes(10), true)));

        // Act + Assert
        assertThatThrownBy(() -> service.resetPassword("token-uji", "PasswordBaru1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sudah pernah dipakai");
    }

    @Test
    @DisplayName("TC-WB-15: resetPassword dengan token kedaluwarsa ditolak (tanpa menunggu 15 menit)")
    void resetPassword_tokenKedaluwarsa_melemparIllegalArgument() {
        // Arrange (Stub): expiredAt diatur 1 menit yang lalu
        when(tokenRepository.findByToken("token-uji"))
                .thenReturn(Optional.of(token(penggunaLokal(), LocalDateTime.now().minusMinutes(1), false)));

        // Act + Assert
        assertThatThrownBy(() -> service.resetPassword("token-uji", "PasswordBaru1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kedaluwarsa");

        verify(penggunaRepository, never()).save(any(Pengguna.class));
    }

    @Test
    @DisplayName("TC-WB-16: resetPassword sukses -> password di-encode, token ditandai terpakai")
    void resetPassword_sukses_passwordBerubahDanTokenTerpakai() {
        // Arrange
        Pengguna pengguna = penggunaLokal();
        PasswordResetToken resetToken = token(pengguna, LocalDateTime.now().plusMinutes(10), false);
        when(tokenRepository.findByToken("token-uji")).thenReturn(Optional.of(resetToken));

        // Act
        service.resetPassword("token-uji", "PasswordBaru1");

        // Assert
        assertThat(pengguna.getPassword()).isEqualTo("ENC:PasswordBaru1");
        assertThat(resetToken.isUsed()).isTrue();
        verify(passwordEncoder).encode("PasswordBaru1"); // Spy
        verify(penggunaRepository).save(pengguna);
        verify(tokenRepository).save(resetToken);
    }

    @Test
    @DisplayName("Batas: password tepat 8 karakter diterima")
    void resetPassword_passwordTepatDelapanKarakter_berhasil() {
        // Arrange
        Pengguna pengguna = penggunaLokal();
        PasswordResetToken resetToken = token(pengguna, LocalDateTime.now().plusMinutes(10), false);
        when(tokenRepository.findByToken("token-uji")).thenReturn(Optional.of(resetToken));

        // Act
        service.resetPassword("token-uji", "Abc12345");

        // Assert
        assertThat(pengguna.getPassword()).isEqualTo("ENC:Abc12345");
        assertThat(resetToken.isUsed()).isTrue();
    }
}
