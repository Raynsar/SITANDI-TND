package com.sipenjam.peminjamanalattu.model;

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
}

