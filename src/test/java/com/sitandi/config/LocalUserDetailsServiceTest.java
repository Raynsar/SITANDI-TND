package com.sitandi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sitandi.model.Pengguna;
import com.sitandi.model.Role;
import com.sitandi.repository.PenggunaRepository;

/**
 * Unit test LocalUserDetailsService (login lokal). Test Double: Stub PenggunaRepository.
 */
@ExtendWith(MockitoExtension.class)
class LocalUserDetailsServiceTest {

    @Mock
    private PenggunaRepository penggunaRepository;

    @InjectMocks
    private LocalUserDetailsService service;

    private Pengguna akun(Role role, boolean aktif, String password) {
        return Pengguna.builder().id(1L).nama("Adifa").username("adifa")
                .password(password).role(role).aktif(aktif).build();
    }

    @Test
    @DisplayName("loadUserByUsername: akun ADMIN aktif -> UserDetails dengan ROLE_ADMIN")
    void akunAdminAktif() {
        when(penggunaRepository.findByUsername("adifa"))
                .thenReturn(Optional.of(akun(Role.ADMIN, true, "$2a$10$hashpanjangsekalibcrypt1234567890")));

        UserDetails hasil = service.loadUserByUsername("adifa");

        assertThat(hasil.getUsername()).isEqualTo("adifa");
        assertThat(hasil.isEnabled()).isTrue();
        assertThat(hasil.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername: akun USER aktif dengan hash pendek -> ROLE_USER")
    void akunUserAktifHashPendek() {
        when(penggunaRepository.findByUsername("adifa")).thenReturn(Optional.of(akun(Role.USER, true, "x")));

        UserDetails hasil = service.loadUserByUsername("adifa");

        assertThat(hasil.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(hasil.getPassword()).isEqualTo("x");
    }

    @Test
    @DisplayName("loadUserByUsername: username tidak ada -> UsernameNotFoundException")
    void usernameTidakAda() {
        when(penggunaRepository.findByUsername("hantu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("hantu"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Pengguna tidak ditemukan");
    }

    @Test
    @DisplayName("loadUserByUsername: akun belum aktif -> UsernameNotFoundException")
    void akunBelumAktif() {
        when(penggunaRepository.findByUsername("adifa")).thenReturn(Optional.of(akun(Role.USER, false, "x")));

        assertThatThrownBy(() -> service.loadUserByUsername("adifa"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Akun belum diaktifkan");
    }
}
