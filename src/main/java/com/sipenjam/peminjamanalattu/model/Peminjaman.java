package com.sipenjam.peminjamanalattu.model;

import java.time.LocalDate;

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
 * Satu transaksi/pengajuan peminjaman alat oleh seorang pengguna.
 */
@Entity
@Table(name = "peminjaman")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Peminjaman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "alat_id", nullable = false)
    private Alat alat;

    /** Pengguna yang mengajukan/meminjam. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "pengguna_id", nullable = false)
    private Pengguna pengguna;

    @Column(nullable = false)
    private LocalDate tanggalPinjam;

    @Column(nullable = false)
    private LocalDate tanggalKembaliRencana;

    /** Diisi saat alat benar-benar dikembalikan. */
    @Column
    private LocalDate tanggalDikembalikan;

    @Column(length = 500)
    private String keperluan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPeminjaman status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private KondisiAlat kondisiSaatKembali;

    @Column(length = 500)
    private String catatanPengembalian;
}
