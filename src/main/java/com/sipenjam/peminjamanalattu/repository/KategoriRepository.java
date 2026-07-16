package com.sipenjam.peminjamanalattu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sipenjam.peminjamanalattu.model.Kategori;

@Repository
public interface KategoriRepository extends JpaRepository<Kategori, Long> {

    Optional<Kategori> findByNama(String nama);
}
