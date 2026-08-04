package com.sipenjam.peminjamanalattu.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.Role;
import com.sipenjam.peminjamanalattu.repository.PenggunaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Mengisi akun awal admin dan user saat aplikasi pertama kali dijalankan.
 * Password di-hash BCrypt. Idempotent: hanya berjalan jika tabel pengguna
 * masih kosong.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PenggunaRepository penggunaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (penggunaRepository.count() > 0) {
            return;
        }

        // aktif(true) wajib eksplisit: field boolean default-nya false, jadi tanpa
        // ini kedua akun seed lahir dalam status menunggu persetujuan dan TIDAK ADA
        // yang bisa login untuk menyetujuinya.
        penggunaRepository.save(Pengguna.builder()
                .nama("Administrator TU")
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .email("admin@sipenjam.local")
                .aktif(true)
                .build());

        penggunaRepository.save(Pengguna.builder()
                .nama("Budi Pengguna")
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .email("user@sipenjam.local")
                .aktif(true)
                .build());
    }
}