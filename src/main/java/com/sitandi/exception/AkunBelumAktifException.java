package com.sitandi.exception;

/**
 * Password benar, tapi akun belum disetujui admin.
 *
 * Sengaja dipisah dari AccessDeniedException agar pesannya bisa spesifik. Ini TIDAK
 * membocorkan keberadaan username, karena hanya dilempar setelah password terbukti
 * benar — orang yang sudah tahu passwordnya memang berhak tahu kenapa ia ditolak.
 */
public class AkunBelumAktifException extends RuntimeException {

    public AkunBelumAktifException(String message) {
        super(message);
    }
}
