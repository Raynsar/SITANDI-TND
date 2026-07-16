package com.sipenjam.peminjamanalattu.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sipenjam.peminjamanalattu.model.Alat;
import com.sipenjam.peminjamanalattu.model.Kategori;
import com.sipenjam.peminjamanalattu.model.KondisiAlat;
import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.Role;
import com.sipenjam.peminjamanalattu.repository.AlatRepository;
import com.sipenjam.peminjamanalattu.repository.KategoriRepository;
import com.sipenjam.peminjamanalattu.repository.PenggunaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Mengisi data awal (akun admin/user, kategori, contoh alat) saat aplikasi
 * pertama kali dijalankan. Password di-hash BCrypt. Idempotent: hanya berjalan
 * jika tabel pengguna masih kosong.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PenggunaRepository penggunaRepository;
    private final KategoriRepository kategoriRepository;
    private final AlatRepository alatRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (penggunaRepository.count() > 0) {
            return; // sudah pernah di-seed
        }

        // aktif(true) wajib eksplisit: field boolean default-nya false, jadi tanpa ini
        // kedua akun seed lahir dalam status menunggu persetujuan dan TIDAK ADA yang
        // bisa login untuk menyetujuinya.
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

        Kategori multimedia = kategoriRepository.save(Kategori.builder().nama("Multimedia").build());
        Kategori kelas = kategoriRepository.save(Kategori.builder().nama("Perangkat Kelas").build());
        Kategori lab = kategoriRepository.save(Kategori.builder().nama("Laboratorium").build());

        alatRepository.save(Alat.builder()
                .nama("Proyektor Epson EB-X06").kategori(multimedia).stok(3)
                .kondisi(KondisiAlat.BAIK).deskripsi("Proyektor untuk presentasi ruang kelas.").build());
        alatRepository.save(Alat.builder()
                .nama("Sound System Portable").kategori(multimedia).stok(2)
                .kondisi(KondisiAlat.BAIK).deskripsi("Speaker portable untuk acara TU.").build());
        alatRepository.save(Alat.builder()
                .nama("Papan Tulis Mobile").kategori(kelas).stok(5)
                .kondisi(KondisiAlat.BAIK).deskripsi("Whiteboard beroda untuk kelas.").build());
        alatRepository.save(Alat.builder()
                .nama("Mikroskop Binokuler").kategori(lab).stok(4)
                .kondisi(KondisiAlat.RUSAK_RINGAN).deskripsi("Mikroskop praktikum laboratorium.").build());
    }
}
