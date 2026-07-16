package com.sipenjam.peminjamanalattu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.model.Role;

@Repository
public interface PenggunaRepository extends JpaRepository<Pengguna, Long> {

    Optional<Pengguna> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Akun yang menunggu persetujuan admin. */
    List<Pengguna> findByAktifFalseOrderByIdDesc();

    List<Pengguna> findAllByOrderByIdDesc();

    /** Dipakai untuk mencegah admin aktif terakhir dinonaktifkan. */
    long countByRoleAndAktifTrue(Role role);
}
