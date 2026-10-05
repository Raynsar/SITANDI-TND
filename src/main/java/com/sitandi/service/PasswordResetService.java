package com.sitandi.service;

/**
 * Layanan untuk fitur lupa password (reset password via email).
 *
 * Hanya untuk akun LOKAL — akun yang masuk via TNDE tidak memiliki email
 * di sistem ini dan password-nya dikelola oleh sistem TNDE sendiri.
 */
public interface PasswordResetService {

    /**
     * Proses request lupa password: validasi email, generate token, kirim email.
     *
     * @param email alamat email yang didaftarkan saat registrasi
     * @throws IllegalArgumentException jika email tidak terdaftar atau milik akun TNDE
     */
    void requestReset(String email);

    /**
     * Validasi token reset dan kembalikan email pemiliknya (untuk ditampilkan di form).
     *
     * @throws IllegalArgumentException jika token tidak valid, kedaluwarsa, atau sudah dipakai
     */
    String validateToken(String token);

    /**
     * Reset password menggunakan token yang valid.
     *
     * @throws IllegalArgumentException jika token tidak valid, kedaluwarsa, atau sudah dipakai
     */
    void resetPassword(String token, String passwordBaru);
}
