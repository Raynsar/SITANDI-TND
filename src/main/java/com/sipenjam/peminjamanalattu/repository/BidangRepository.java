package com.sipenjam.peminjamanalattu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sipenjam.peminjamanalattu.model.Bidang;

@Repository
public interface BidangRepository extends JpaRepository<Bidang, Long> {

    List<Bidang> findByOpdIdOrderByNamaBidang(Long opdId);

    long countByOpdId(Long opdId);
}

