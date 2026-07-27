package com.sipenjam.peminjamanalattu.exception;

/**
 * Exception ketika login ke API eksternal TNDE gagal.
 * Digunakan untuk membedakan error login lokal (AuthenticationException) dengan
 * error koneksi/response dari API TNDE.
 */
public class TndeLoginException extends RuntimeException {

    public TndeLoginException(String message) {
        super(message);
    }

    public TndeLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
