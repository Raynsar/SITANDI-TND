package com.sitandi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sitandi.model.Pengguna;
import com.sitandi.model.Role;

public interface PenggunaRepository extends JpaRepository<Pengguna, Long> {

    Optional<Pengguna> findByUsername(String username);

    Optional<Pengguna> findByEmail(String email);

    boolean existsByUsername(String username);

    /** Akun yang menunggu persetujuan admin. */
    List<Pengguna> findByAktifFalseOrderByIdDesc();

    List<Pengguna> findAllByOrderByIdDesc();

    /** Dipakai untuk mencegah admin aktif terakhir dinonaktifkan. */
    long countByRoleAndAktifTrue(Role role);
}
