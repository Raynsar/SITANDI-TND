package com.sitandi.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sitandi.model.PasswordResetToken;
import com.sitandi.model.Pengguna;
import com.sitandi.repository.PasswordResetTokenRepository;
import com.sitandi.repository.PenggunaRepository;
import com.sitandi.service.PasswordResetService;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;

/**
 * Implementasi reset password via email menggunakan Mailtrap SDK.
 *
 * Token reset berlaku 15 menit dan hanya sekali pakai. Email hanya dikirim
 * ke akun LOKAL (bukan TNDE) — akun TNDE tidak punya email di sistem ini.
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

    /** Panjang token reset dalam byte (48 byte → 64 karakter Base64). */
    private static final int TOKEN_BYTE_LENGTH = 48;

    /** Masa berlaku token reset: 15 menit. */
    private static final int TOKEN_EXPIRY_MINUTES = 15;

    private final PenggunaRepository penggunaRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailtrapClient mailtrapClient;
    private final String appBaseUrl;

    public PasswordResetServiceImpl(PenggunaRepository penggunaRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    PasswordEncoder passwordEncoder,
                                    @Value("${mailtrap.api-token:}") String mailtrapToken,
                                    @Value("${app.base-url:http://localhost:8080}") String appBaseUrl) {
        this.penggunaRepository = penggunaRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.appBaseUrl = appBaseUrl;

        // Jangan fail-fast di constructor agar aplikasi tetap bisa start
        if (StringUtils.hasText(mailtrapToken)) {
            MailtrapConfig config = new MailtrapConfig.Builder()
                    .token(mailtrapToken)
                    .build();
            this.mailtrapClient = MailtrapClientFactory.createMailtrapClient(config);
        } else {
            this.mailtrapClient = null;
        }
    }

    @Override
    @Transactional
    public void requestReset(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email tidak boleh kosong");
        }

        Pengguna pengguna = penggunaRepository.findByEmail(email.trim())
                .orElse(null);

        if (pengguna == null) {
            log.warn("[PasswordReset] Email '{}' tidak terdaftar", email);
            throw new IllegalArgumentException("Email tidak ditemukan atau bukan akun lokal");
        }

        /*
         * Akun TNDE: password dikelola oleh sistem TNDE, bukan SITANDI.
         * Akun TNDE dibuat oleh sinkronkanPengguna() tanpa email, tapi karena
         * kita lookup by email dan hanya akun lokal yang punya email, secara
         * logika seharusnya tidak sampai sini untuk akun TNDE. Tetap ada guard
         * sebagai defense-in-depth untuk kasus edge (misal admin mengedit data
         * manual di DB).
         */
        if (pengguna.getEmail() == null || pengguna.getEmail().isBlank()) {
            log.warn("[PasswordReset] Akun '{}' tidak punya email (kemungkinan TNDE)",
                    pengguna.getUsername());
            throw new IllegalArgumentException("Email tidak ditemukan atau bukan akun lokal");
        }

        // Hapus token lama milik pengguna ini agar tidak menumpuk
        tokenRepository.deleteByPengguna(pengguna);

        // Generate token baru
        String tokenValue = generateSecureToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenValue)
                .pengguna(pengguna)
                .expiredAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                .build();
        tokenRepository.save(resetToken);

        // Kirim email
        String resetLink = appBaseUrl + "/reset-password?token=" + tokenValue;
        kirimEmailReset(pengguna, resetLink);

        log.info("[PasswordReset] Token reset dibuat dan email dikirim ke '{}' untuk user '{}'",
                email, pengguna.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public String validateToken(String token) {
        PasswordResetToken resetToken = findValidToken(token);
        return resetToken.getPengguna().getEmail();
    }

    @Override
    @Transactional
    public void resetPassword(String token, String passwordBaru) {
        if (!StringUtils.hasText(passwordBaru)) {
            throw new IllegalArgumentException("Password baru tidak boleh kosong");
        }
        if (passwordBaru.length() < 8) {
            throw new IllegalArgumentException("Password baru minimal 8 karakter");
        }

        PasswordResetToken resetToken = findValidToken(token);
        Pengguna pengguna = resetToken.getPengguna();

        // Update password
        pengguna.setPassword(passwordEncoder.encode(passwordBaru));
        penggunaRepository.save(pengguna);

        // Invalidate token setelah dipakai
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("[PasswordReset] Password berhasil direset untuk user '{}'", pengguna.getUsername());
    }

    /**
     * Cari dan validasi token. Throw exception yang informatif jika gagal.
     */
    private PasswordResetToken findValidToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token reset tidak boleh kosong");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Token reset tidak valid. Silakan request ulang reset password."));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException(
                    "Token reset sudah pernah dipakai. Silakan request ulang reset password.");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException(
                    "Token reset sudah kedaluwarsa (berlaku 15 menit). Silakan request ulang reset password.");
        }

        return resetToken;
    }

    /**
     * Kirim email berisi link reset password via Mailtrap SDK.
     */
    private void kirimEmailReset(Pengguna pengguna, String resetLink) {
        if (mailtrapClient == null) {
            throw new IllegalStateException("Gagal mengirim email reset password. Konfigurasi Mailtrap belum diatur.");
        }
        try {
            MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address("hello@demomailtrap.co", "SITANDI"))
                    .to(List.of(new Address(pengguna.getEmail(), pengguna.getNama())))
                    .subject("Reset Password SITANDI")
                    .text("Halo " + pengguna.getNama() + ",\n\n"
                            + "Anda menerima email ini karena ada permintaan reset password untuk akun SITANDI Anda.\n\n"
                            + "Klik link berikut untuk membuat password baru:\n"
                            + resetLink + "\n\n"
                            + "Link ini berlaku selama 15 menit dan hanya bisa digunakan sekali.\n\n"
                            + "Jika Anda tidak merasa meminta reset password, abaikan email ini.\n\n"
                            + "Salam,\n"
                            + "Tim SITANDI")
                    .category("Password Reset")
                    .build();

            mailtrapClient.send(mail);
            log.info("[PasswordReset] Email reset berhasil dikirim ke '{}'", pengguna.getEmail());
        } catch (Exception ex) {
            log.error("[PasswordReset] Gagal mengirim email reset ke '{}': {}",
                    pengguna.getEmail(), ex.getMessage(), ex);
            throw new IllegalStateException("Gagal mengirim email reset password. Pastikan konfigurasi Mailtrap benar dan coba lagi nanti.");
        }
    }

    /**
     * Generate token acak yang aman secara kriptografi.
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
