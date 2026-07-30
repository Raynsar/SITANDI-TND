package com.sipenjam.peminjamanalattu.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.repository.PenggunaRepository;

import lombok.RequiredArgsConstructor;

/**
 * UserDetailsService untuk autentikasi lokal (fallback).
 *
 * Memuat user dari tabel pengguna di database lokal, bukan dari TNDE API.
 * Dipakai oleh DaoAuthenticationProvider sebagai fallback ketika
 * TndeAuthenticationProvider gagal (karena TNDE API tidak reachable atau
 * kredensial tidak terdaftar di TNDE).
 */
@Service
@RequiredArgsConstructor
public class LocalUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(LocalUserDetailsService.class);

    private final PenggunaRepository penggunaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("[LocalUserDetails] Mencari user '{}' di database lokal...", username);

        Pengguna pengguna = penggunaRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[LocalUserDetails] User '{}' TIDAK DITEMUKAN di database lokal", username);
                    return new UsernameNotFoundException(
                            "Pengguna tidak ditemukan: " + username);
                });

        if (!pengguna.isAktif()) {
            log.warn("[LocalUserDetails] User '{}' DITEMUKAN tapi belum aktif (aktif=false)", username);
            throw new UsernameNotFoundException("Akun belum diaktifkan: " + username);
        }

        log.info("[LocalUserDetails] User '{}' DITEMUKAN di DB | role={} | aktif={} | pw_hash_prefix={}",
                username, pengguna.getRole(), pengguna.isAktif(),
                pengguna.getPassword().substring(0, Math.min(20, pengguna.getPassword().length())));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + pengguna.getRole().name());

        return new User(
                pengguna.getUsername(),
                pengguna.getPassword(),
                pengguna.isAktif(),
                true,
                true,
                true,
                List.of(authority));
    }
}

