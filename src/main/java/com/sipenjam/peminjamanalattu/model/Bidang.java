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
 * Bidang dalam suatu OPD.
 * Contoh: Bidang Infrastruktur Teknologi Informasi.
 */
@Entity
@Table(name = "bidang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bidang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opd_id", nullable = false)
    private Opd opd;

    @Column(nullable = false)
    private String namaBidang;
}

