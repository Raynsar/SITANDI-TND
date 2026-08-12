package com.sipenjam.peminjamanalattu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sipenjam.peminjamanalattu.model.JabatanStruktur;

@Repository
public interface JabatanStrukturRepository extends JpaRepository<JabatanStruktur, Long> {

    List<JabatanStruktur> findByBidangIdOrderByJabatan(Long bidangId);

    long countByBidangId(Long bidangId);
}

