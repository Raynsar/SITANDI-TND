package com.sitandi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sitandi.model.PasswordResetToken;
import com.sitandi.model.Pengguna;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /** Hapus semua token lama milik pengguna tertentu (bersih-bersih saat generate token baru). */
    void deleteByPengguna(Pengguna pengguna);
}
