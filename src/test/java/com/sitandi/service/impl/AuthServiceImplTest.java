package com.sitandi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitandi.dto.GantiPasswordRequestDto;
import com.sitandi.dto.PenggunaDto;
import com.sitandi.dto.RegisterRequestDto;
import com.sitandi.dto.TndeLoginResponseDto;
import com.sitandi.dto.TndeUserDataDto;
import com.sitandi.exception.ResourceNotFoundException;
import com.sitandi.exception.TndeLoginException;
import com.sitandi.model.Pengguna;
import com.sitandi.model.Role;
import com.sitandi.repository.PenggunaRepository;

/**
 * Unit test AuthServiceImpl: Register, Ganti Password, Kelola Pengguna, dan Login TNDE.
 *
 * Test Double:
 * - Stub  : PenggunaRepository (nilai kembalian disiapkan).
 * - Mock  : PenggunaRepository dan RestTemplate (interaksi diverifikasi).
 * - Fake  : FakePasswordEncoder.
 * - Spy   : spy(FakePasswordEncoder).
 * - Dummy : RestTemplate pada test non-TNDE (dibutuhkan konstruktor, tidak pernah dipakai).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String TNDE_URL = "http://tnde.test/api/login";

    @Mock
    private PenggunaRepository penggunaRepository;

    @Mock
    private RestTemplate restTemplate;

    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl service;

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
        service = new AuthServiceImpl(penggunaRepository, passwordEncoder, restTemplate,
                new ObjectMapper(), TNDE_URL);
    }

    private Pengguna pengguna(Long id, String username, Role role, boolean aktif) {
        return Pengguna.builder()
                .id(id)
                .nama("Nama " + username)
                .username(username)
                .password("ENC:lama12345")
                .email(username + "@qa.local")
                .role(role)
                .aktif(aktif)
                .build();
    }

    private RegisterRequestDto registerRequest(String password, String konfirmasi) {
        return RegisterRequestDto.builder()
                .nama("  Adifa Arrayan  ")
                .username("  adifa.ep01  ")
                .email("  adifa@qa.local  ")
                .password(password)
                .konfirmasiPassword(konfirmasi)
                .build();
    }

    // ================= getByUsername =================

    @Test
    @DisplayName("getByUsername: username ada -> pengguna dikembalikan")
    void getByUsername_ada_mengembalikanPengguna() {
        Pengguna p = pengguna(1L, "adifa", Role.USER, true);
        when(penggunaRepository.findByUsername("adifa")).thenReturn(Optional.of(p));

        assertThat(service.getByUsername("adifa")).isSameAs(p);
    }

    @Test
    @DisplayName("getByUsername: username tidak ada -> UsernameNotFoundException")
    void getByUsername_tidakAda_melemparException() {
        when(penggunaRepository.findByUsername("hantu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByUsername("hantu"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("hantu");
    }

    // ================= register =================

    @Test
    @DisplayName("register valid: akun dibuat role USER, aktif=false, password di-encode, data di-trim")
    void register_dataValid_akunPendingRoleUser() {
        // Arrange
        when(penggunaRepository.existsByUsername("  adifa.ep01  ")).thenReturn(false);
        when(penggunaRepository.save(any(Pengguna.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PenggunaDto hasil = service.register(registerRequest("Rahasia123", "Rahasia123"));

        // Assert
        assertThat(hasil.getRole()).isEqualTo("USER");
        assertThat(hasil.isAktif()).isFalse();
        assertThat(hasil.getUsername()).isEqualTo("adifa.ep01");
        assertThat(hasil.getNama()).isEqualTo("Adifa Arrayan");
        assertThat(hasil.getEmail()).isEqualTo("adifa@qa.local");

        ArgumentCaptor<Pengguna> captor = ArgumentCaptor.forClass(Pengguna.class);
        verify(penggunaRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("ENC:Rahasia123");
        verify(passwordEncoder).encode("Rahasia123"); // Spy
    }

    @Test
    @DisplayName("register: konfirmasi password tidak sama ditolak, tidak ada yang disimpan")
    void register_konfirmasiBeda_ditolak() {
        assertThatThrownBy(() -> service.register(registerRequest("Rahasia123", "Beda12345")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Konfirmasi password tidak cocok");

        verifyNoInteractions(penggunaRepository, restTemplate); // restTemplate = Dummy
    }

    @Test
    @DisplayName("register: username sudah dipakai ditolak, tidak ada yang disimpan")
    void register_usernameDuplikat_ditolak() {
        when(penggunaRepository.existsByUsername("  adifa.ep01  ")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registerRequest("Rahasia123", "Rahasia123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username sudah dipakai");

        verify(penggunaRepository, never()).save(any(Pengguna.class));
    }

    // ================= gantiPassword =================

    private GantiPasswordRequestDto gantiRequest(String lama, String baru, String konfirmasi) {
        return GantiPasswordRequestDto.builder()
                .passwordLama(lama).passwordBaru(baru).konfirmasiPasswordBaru(konfirmasi).build();
    }

    @Test
    @DisplayName("gantiPassword valid: password baru tersimpan ter-encode")
    void gantiPassword_valid_passwordBerubah() {
        Pengguna p = pengguna(1L, "adifa", Role.USER, true); // password "ENC:lama12345"
        when(penggunaRepository.findByUsername("adifa")).thenReturn(Optional.of(p));

        service.gantiPassword(gantiRequest("lama12345", "baru12345", "baru12345"), "adifa");

        assertThat(p.getPassword()).isEqualTo("ENC:baru12345");
        verify(penggunaRepository).save(p);
    }

    @Test
    @DisplayName("gantiPassword: konfirmasi password baru tidak cocok ditolak")
    void gantiPassword_konfirmasiBeda_ditolak() {
        assertThatThrownBy(() -> service.gantiPassword(gantiRequest("lama12345", "baru12345", "lain12345"), "adifa"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Konfirmasi password baru tidak cocok");

        verifyNoInteractions(penggunaRepository);
    }

    @Test
    @DisplayName("gantiPassword: password lama salah ditolak")
    void gantiPassword_passwordLamaSalah_ditolak() {
        when(penggunaRepository.findByUsername("adifa"))
                .thenReturn(Optional.of(pengguna(1L, "adifa", Role.USER, true)));

        assertThatThrownBy(() -> service.gantiPassword(gantiRequest("salah12345", "baru12345", "baru12345"), "adifa"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password lama salah");

        verify(penggunaRepository, never()).save(any(Pengguna.class));
    }

    @Test
    @DisplayName("gantiPassword: password baru sama dengan password lama ditolak")
    void gantiPassword_passwordBaruSamaDenganLama_ditolak() {
        when(penggunaRepository.findByUsername("adifa"))
                .thenReturn(Optional.of(pengguna(1L, "adifa", Role.USER, true)));

        assertThatThrownBy(() -> service.gantiPassword(gantiRequest("lama12345", "lama12345", "lama12345"), "adifa"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tidak boleh sama");
    }

    @Test
    @DisplayName("gantiPassword: pengguna tidak ditemukan -> UsernameNotFoundException")
    void gantiPassword_penggunaTidakAda_melemparException() {
        when(penggunaRepository.findByUsername("hantu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gantiPassword(gantiRequest("a12345678", "b12345678", "b12345678"), "hantu"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ================= daftar pengguna =================

    @Test
    @DisplayName("daftarMenungguAktivasi memetakan akun pending ke DTO")
    void daftarMenungguAktivasi_memetakanKeDto() {
        when(penggunaRepository.findByAktifFalseOrderByIdDesc())
                .thenReturn(List.of(pengguna(2L, "pending1", Role.USER, false)));

        List<PenggunaDto> hasil = service.daftarMenungguAktivasi();

        assertThat(hasil).hasSize(1);
        assertThat(hasil.get(0).getUsername()).isEqualTo("pending1");
        assertThat(hasil.get(0).isAktif()).isFalse();
    }

    @Test
    @DisplayName("daftarSemuaPengguna memetakan semua akun ke DTO")
    void daftarSemuaPengguna_memetakanKeDto() {
        when(penggunaRepository.findAllByOrderByIdDesc()).thenReturn(List.of(
                pengguna(2L, "user1", Role.USER, true), pengguna(1L, "admin", Role.ADMIN, true)));

        List<PenggunaDto> hasil = service.daftarSemuaPengguna();

        assertThat(hasil).extracting(PenggunaDto::getUsername).containsExactly("user1", "admin");
        assertThat(hasil).extracting(PenggunaDto::getRole).containsExactly("USER", "ADMIN");
    }

    // ================= aktifkan =================

    @Test
    @DisplayName("aktifkan: akun pending menjadi aktif")
    void aktifkan_akunPending_menjadiAktif() {
        Pengguna p = pengguna(5L, "pending1", Role.USER, false);
        when(penggunaRepository.findById(5L)).thenReturn(Optional.of(p));
        when(penggunaRepository.save(p)).thenReturn(p);

        PenggunaDto hasil = service.aktifkan(5L);

        assertThat(hasil.isAktif()).isTrue();
        assertThat(p.isAktif()).isTrue();
    }

    @Test
    @DisplayName("aktifkan: id tidak ditemukan -> ResourceNotFoundException")
    void aktifkan_idTidakAda_melemparException() {
        when(penggunaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.aktifkan(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ================= nonaktifkan =================

    @Test
    @DisplayName("nonaktifkan: akun USER aktif menjadi nonaktif")
    void nonaktifkan_akunUser_menjadiNonaktif() {
        Pengguna p = pengguna(5L, "user1", Role.USER, true);
        when(penggunaRepository.findById(5L)).thenReturn(Optional.of(p));
        when(penggunaRepository.save(p)).thenReturn(p);

        PenggunaDto hasil = service.nonaktifkan(5L, "admin");

        assertThat(hasil.isAktif()).isFalse();
    }

    @Test
    @DisplayName("nonaktifkan: admin boleh dinonaktifkan bila masih ada admin aktif lain")
    void nonaktifkan_adminDenganAdminLain_berhasil() {
        Pengguna p = pengguna(5L, "admin2", Role.ADMIN, true);
        when(penggunaRepository.findById(5L)).thenReturn(Optional.of(p));
        when(penggunaRepository.countByRoleAndAktifTrue(Role.ADMIN)).thenReturn(2L);
        when(penggunaRepository.save(p)).thenReturn(p);

        PenggunaDto hasil = service.nonaktifkan(5L, "admin");

        assertThat(hasil.isAktif()).isFalse();
    }

    @Test
    @DisplayName("nonaktifkan: akun sendiri ditolak")
    void nonaktifkan_akunSendiri_ditolak() {
        when(penggunaRepository.findById(1L)).thenReturn(Optional.of(pengguna(1L, "admin", Role.ADMIN, true)));

        assertThatThrownBy(() -> service.nonaktifkan(1L, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tidak bisa menonaktifkan akun Anda sendiri");

        verify(penggunaRepository, never()).save(any(Pengguna.class));
    }

    @Test
    @DisplayName("nonaktifkan: admin aktif terakhir ditolak")
    void nonaktifkan_adminTerakhir_ditolak() {
        when(penggunaRepository.findById(5L)).thenReturn(Optional.of(pengguna(5L, "admin2", Role.ADMIN, true)));
        when(penggunaRepository.countByRoleAndAktifTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.nonaktifkan(5L, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("admin aktif terakhir");
    }

    @Test
    @DisplayName("nonaktifkan: id tidak ditemukan -> ResourceNotFoundException")
    void nonaktifkan_idTidakAda_melemparException() {
        when(penggunaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.nonaktifkan(99L, "admin")).isInstanceOf(ResourceNotFoundException.class);
    }

    // ================= hapusPengguna =================

    @Test
    @DisplayName("hapusPengguna: akun USER dihapus permanen")
    void hapusPengguna_akunUser_dihapus() {
        Pengguna p = pengguna(5L, "user1", Role.USER, true);
        when(penggunaRepository.findById(5L)).thenReturn(Optional.of(p));

        service.hapusPengguna(5L, "admin");

        verify(penggunaRepository).delete(p);
    }

    @Test
    @DisplayName("hapusPengguna: akun sendiri ditolak")
    void hapusPengguna_akunSendiri_ditolak() {
        when(penggunaRepository.findById(1L)).thenReturn(Optional.of(pengguna(1L, "admin", Role.ADMIN, true)));

        assertThatThrownBy(() -> service.hapusPengguna(1L, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tidak bisa menghapus akun Anda sendiri");

        verify(penggunaRepository, never()).delete(any(Pengguna.class));
    }

    @Test
    @DisplayName("hapusPengguna: admin aktif terakhir ditolak")
    void hapusPengguna_adminTerakhir_ditolak() {
        when(penggunaRepository.findById(5L)).thenReturn(Optional.of(pengguna(5L, "admin2", Role.ADMIN, true)));
        when(penggunaRepository.countByRoleAndAktifTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.hapusPengguna(5L, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("admin aktif terakhir");

        verify(penggunaRepository, never()).delete(any(Pengguna.class));
    }

    @Test
    @DisplayName("hapusPengguna: id tidak ditemukan -> ResourceNotFoundException")
    void hapusPengguna_idTidakAda_melemparException() {
        when(penggunaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.hapusPengguna(99L, "admin")).isInstanceOf(ResourceNotFoundException.class);
    }

    // ================= loginTnde =================

    private TndeLoginResponseDto responseTnde(String username, String levelName) {
        TndeUserDataDto user = TndeUserDataDto.builder()
                .username(username).nama("Nama TNDE").levelName(levelName).build();
        return TndeLoginResponseDto.builder()
                .success(true)
                .message("OK")
                .data(TndeLoginResponseDto.TndeLoginDataDto.builder().token("jwt-tnde").user(user).build())
                .build();
    }

    private void stubPostTnde(TndeLoginResponseDto response) {
        when(restTemplate.postForObject(eq(TNDE_URL), any(HttpEntity.class), eq(TndeLoginResponseDto.class)))
                .thenReturn(response);
    }

    @Test
    @DisplayName("loginTnde sukses, pengguna baru: dibuat aktif dengan role ADMIN dari level_name")
    void loginTnde_penggunaBaruAdmin_dibuatAktif() {
        stubPostTnde(responseTnde("tnde1", "Administrator"));
        when(penggunaRepository.findByUsername("tnde1")).thenReturn(Optional.empty());
        when(penggunaRepository.save(any(Pengguna.class))).thenAnswer(inv -> inv.getArgument(0));

        TndeLoginResponseDto hasil = service.loginTnde("tnde1", "rahasia");

        assertThat(hasil.isSuccess()).isTrue();
        ArgumentCaptor<Pengguna> captor = ArgumentCaptor.forClass(Pengguna.class);
        verify(penggunaRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(captor.getValue().isAktif()).isTrue();
        assertThat(captor.getValue().getUsername()).isEqualTo("tnde1");
        verify(passwordEncoder).encode(any(String.class)); // password acak di-encode (Spy)
    }

    @Test
    @DisplayName("loginTnde sukses, pengguna lama: nama dan role disinkronkan")
    void loginTnde_penggunaLama_disinkronkan() {
        Pengguna lama = pengguna(7L, "tnde1", Role.ADMIN, true);
        stubPostTnde(responseTnde("tnde1", "Staff Biasa"));
        when(penggunaRepository.findByUsername("tnde1")).thenReturn(Optional.of(lama));
        when(penggunaRepository.save(lama)).thenReturn(lama);

        service.loginTnde("tnde1", "rahasia");

        assertThat(lama.getNama()).isEqualTo("Nama TNDE");
        assertThat(lama.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("loginTnde: level_name null dipetakan ke role USER")
    void loginTnde_levelNameNull_roleUser() {
        stubPostTnde(responseTnde("tnde2", null));
        when(penggunaRepository.findByUsername("tnde2")).thenReturn(Optional.empty());
        when(penggunaRepository.save(any(Pengguna.class))).thenAnswer(inv -> inv.getArgument(0));

        service.loginTnde("tnde2", "rahasia");

        ArgumentCaptor<Pengguna> captor = ArgumentCaptor.forClass(Pengguna.class);
        verify(penggunaRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("loginTnde: server tidak terjangkau -> TndeLoginException")
    void loginTnde_serverTidakTerjangkau_melemparException() {
        when(restTemplate.postForObject(eq(TNDE_URL), any(HttpEntity.class), eq(TndeLoginResponseDto.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        assertThatThrownBy(() -> service.loginTnde("tnde1", "rahasia"))
                .isInstanceOf(TndeLoginException.class)
                .hasMessageContaining("tidak dapat dihubungi");
    }

    @Test
    @DisplayName("loginTnde: HTTP error dengan body JSON berisi pesan -> pesan TNDE diteruskan")
    void loginTnde_httpErrorBodyJson_pesanDiteruskan() {
        byte[] body = "{\"success\":false,\"message\":\"Password salah\"}".getBytes(StandardCharsets.UTF_8);
        when(restTemplate.postForObject(eq(TNDE_URL), any(HttpEntity.class), eq(TndeLoginResponseDto.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized",
                        HttpHeaders.EMPTY, body, StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.loginTnde("tnde1", "salah"))
                .isInstanceOf(TndeLoginException.class)
                .hasMessage("Password salah");
    }

    @Test
    @DisplayName("loginTnde: HTTP error dengan body bukan JSON -> pesan generik")
    void loginTnde_httpErrorBodyBukanJson_pesanGenerik() {
        byte[] body = "<html>error</html>".getBytes(StandardCharsets.UTF_8);
        when(restTemplate.postForObject(eq(TNDE_URL), any(HttpEntity.class), eq(TndeLoginResponseDto.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
                        HttpHeaders.EMPTY, body, StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.loginTnde("tnde1", "x"))
                .isInstanceOf(TndeLoginException.class)
                .hasMessage("Gagal memproses respons dari server TNDE");
    }

    @Test
    @DisplayName("loginTnde: HTTP error dengan body kosong -> pesan generik")
    void loginTnde_httpErrorBodyKosong_pesanGenerik() {
        when(restTemplate.postForObject(eq(TNDE_URL), any(HttpEntity.class), eq(TndeLoginResponseDto.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_GATEWAY, "Bad Gateway",
                        HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.loginTnde("tnde1", "x"))
                .isInstanceOf(TndeLoginException.class)
                .hasMessage("Gagal memproses respons dari server TNDE");
    }

    @Test
    @DisplayName("loginTnde: RestClientException umum -> pesan generik")
    void loginTnde_restClientException_pesanGenerik() {
        when(restTemplate.postForObject(eq(TNDE_URL), any(HttpEntity.class), eq(TndeLoginResponseDto.class)))
                .thenThrow(new RestClientException("gagal parse"));

        assertThatThrownBy(() -> service.loginTnde("tnde1", "x"))
                .isInstanceOf(TndeLoginException.class)
                .hasMessage("Gagal memproses respons dari server TNDE");
    }

    @Test
    @DisplayName("loginTnde: respons null -> TndeLoginException")
    void loginTnde_responsNull_melemparException() {
        stubPostTnde(null);

        assertThatThrownBy(() -> service.loginTnde("tnde1", "x"))
                .isInstanceOf(TndeLoginException.class)
                .hasMessageContaining("tidak dapat dihubungi");
    }

    @Test
    @DisplayName("loginTnde: success=false -> pesan dari TNDE")
    void loginTnde_successFalse_pesanDariTnde() {
        stubPostTnde(TndeLoginResponseDto.builder().success(false).message("Kredensial salah").build());

        assertThatThrownBy(() -> service.loginTnde("tnde1", "x"))
                .isInstanceOf(TndeLoginException.class)
                .hasMessage("Kredensial salah");
    }

    @Test
    @DisplayName("loginTnde: data atau user null -> respons tidak lengkap")
    void loginTnde_dataTidakLengkap_melemparException() {
        stubPostTnde(TndeLoginResponseDto.builder().success(true).message("OK").data(null).build());

        assertThatThrownBy(() -> service.loginTnde("tnde1", "x"))
                .isInstanceOf(TndeLoginException.class)
                .hasMessage("Respons login TNDE tidak lengkap");

        verify(penggunaRepository, never()).save(any(Pengguna.class));
    }
}
