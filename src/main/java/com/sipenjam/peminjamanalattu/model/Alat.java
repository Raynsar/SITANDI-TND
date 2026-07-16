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
 * Alat/sarana yang dapat dipinjam beserta stok dan kondisinya.
 */
@Entity
@Table(name = "alat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "kategori_id")
    private Kategori kategori;

    @Column(nullable = false)
    private int stok;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KondisiAlat kondisi;

    @Column(length = 500)
    private String deskripsi;
}
