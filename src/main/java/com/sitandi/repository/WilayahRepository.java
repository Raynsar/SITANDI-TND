package com.sitandi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sitandi.model.TipeWilayah;
import com.sitandi.model.Wilayah;

@Repository
public interface WilayahRepository extends JpaRepository<Wilayah, Long> {

    /** Ambil semua root node (parent = null) — dalam hal ini Provinsi. */
    List<Wilayah> findByParentIsNullOrderByNama();

    /** Ambil child langsung dari suatu node (lazy-load). */
    List<Wilayah> findByParentIdOrderByNama(Long parentId);

    List<Wilayah> findByTipeOrderByNama(TipeWilayah tipe);
}

