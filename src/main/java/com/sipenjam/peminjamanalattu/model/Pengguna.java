package com.sipenjam.peminjamanalattu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Akun pengguna aplikasi (admin TU maupun pengguna biasa/pemohon).
 */
@Entity
@Table(name = "pengguna")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pengguna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(nullable = false, unique = true)
    private String username;

    /** Password sudah di-hash BCrypt, tidak pernah disimpan plaintext. */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column
    private String email;

    /**
     * Akun sudah disetujui admin dan boleh login.
     *
     * Pendaftaran mandiri selalu membuat akun dengan aktif=false sampai admin
     * menyetujui lewat /admin/pengguna.
     *
     * DEFAULT TRUE di columnDefinition itu khusus untuk migrasi: saat kolom ini
     * ditambahkan ddl-auto=update ke tabel yang sudah berisi akun lama, baris lama
     * ikut terisi TRUE sehingga akun yang sudah ada tidak ikut terkunci. Tanpa
     * default, MySQL mengisi baris lama dengan 0 dan semua orang gagal login.
     *
     * Konsekuensinya default ini juga berlaku di level DB, jadi kode yang membuat
     * akun WAJIB menyetel aktif secara eksplisit — jangan mengandalkan default.
     */
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean aktif;
}
