package com.sipenjam.peminjamanalattu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Kategori/pengelompokan alat (mis. Multimedia, Kelas, Laboratorium).
 */
@Entity
@Table(name = "kategori")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kategori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nama;
}
