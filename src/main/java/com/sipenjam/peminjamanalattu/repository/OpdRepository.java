package com.sipenjam.peminjamanalattu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sipenjam.peminjamanalattu.model.Opd;

@Repository
public interface OpdRepository extends JpaRepository<Opd, Long> {

    List<Opd> findByWilayahIdOrderByNamaOpd(Long wilayahId);

    Optional<Opd> findByKodeOpd(String kodeOpd);

    long countByWilayahId(Long wilayahId);
}

