package com.sitandi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Jabatan dalam struktur organisasi bidang.
 * Terhubung ke user dummy via namaDummy (belum ada tabel user terpisah).
 */
@Entity
@Table(name = "jabatan_struktur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JabatanStruktur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidang_id", nullable = false)
    private Bidang bidang;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Jabatan jabatan;

    @Column(nullable = false)
    private String namaDummy;

    /**
     * Username dummy untuk tampilan (kolom Username di tabel Daftar Personal).
     * Bukan akun login sungguhan dan tidak terhubung ke tabel User. Selalu
     * digenerate oleh seeder mengikuti format: inisial depan + nama belakang,
     * lowercase, tanpa spasi (mis. "Ahmad Fauzi" -> "afauzi"). Nullable=false
     * setelah seeder berjalan; data lama tanpa nilai akan dipicu reseed.
     */
    @Column(nullable = false, length = 100)
    private String username;
}

