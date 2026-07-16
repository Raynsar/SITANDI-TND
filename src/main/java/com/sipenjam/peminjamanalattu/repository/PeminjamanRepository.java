package com.sipenjam.peminjamanalattu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sipenjam.peminjamanalattu.model.Peminjaman;
import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.StatusPeminjaman;

/**
 * Repository data peminjaman. Query method diturunkan otomatis oleh Spring Data JPA.
 */
@Repository
public interface PeminjamanRepository extends JpaRepository<Peminjaman, Long> {

    List<Peminjaman> findByPenggunaOrderByIdDesc(Pengguna pengguna);

    List<Peminjaman> findByStatusOrderByIdDesc(StatusPeminjaman status);

    List<Peminjaman> findByStatusInOrderByIdDesc(List<StatusPeminjaman> statuses);

    List<Peminjaman> findAllByOrderByIdDesc();

    long countByStatus(StatusPeminjaman status);

    long countByStatusIn(List<StatusPeminjaman> statuses);
}
