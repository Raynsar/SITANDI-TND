package com.sitandi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sitandi.model.JabatanStruktur;

public interface JabatanStrukturRepository extends JpaRepository<JabatanStruktur, Long> {

    List<JabatanStruktur> findByBidangIdOrderByJabatan(Long bidangId);

    long countByBidangId(Long bidangId);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    List<JabatanStruktur> findByNamaDummyContainingIgnoreCaseOrNipContainingIgnoreCaseOrJabatanIn(
            String namaDummy, String nip, List<com.sitandi.model.Jabatan> jabatans);
}

