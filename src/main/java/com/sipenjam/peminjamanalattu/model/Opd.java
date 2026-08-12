package com.sipenjam.peminjamanalattu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Organisasi Perangkat Daerah (OPD) di bawah Kabupaten/Kota.
 * Contoh: Dinas Komunikasi dan Informatika Kabupaten Malang.
 */
@Entity
@Table(name = "opd")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wilayah_id", nullable = false)
    private Wilayah wilayah;

    @Column(nullable = false)
    private String namaOpd;

    @Column(nullable = false, unique = true, length = 10)
    private String kodeOpd;
}

