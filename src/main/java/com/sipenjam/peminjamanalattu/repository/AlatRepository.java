package com.sipenjam.peminjamanalattu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sipenjam.peminjamanalattu.model.Alat;

@Repository
public interface AlatRepository extends JpaRepository<Alat, Long> {
}
