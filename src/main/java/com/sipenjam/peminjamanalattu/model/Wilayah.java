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
 * Wilayah dalam struktur organisasi (Provinsi, Kabupaten, Kota).
 * Berelasi self-referencing (parentId -> id) untuk membentuk hierarki tree.
 */
@Entity
@Table(name = "wilayah")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wilayah {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipeWilayah tipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Wilayah parent;
}

