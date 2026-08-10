package com.sitandi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sitandi.model.Bidang;

@Repository
public interface BidangRepository extends JpaRepository<Bidang, Long> {

    List<Bidang> findByOpdIdOrderByNamaBidang(Long opdId);

    long countByOpdId(Long opdId);

    List<Bidang> findByNamaBidangContainingIgnoreCase(String namaBidang);
}

