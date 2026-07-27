package com.sipenjam.peminjamanalattu.exception;

/**
 * Exception ketika komunikasi dengan server TNDE gagal (timeout / connection error),
 * bukan karena autentikasi/login.
 */
public class TndeServerException extends RuntimeException {

    public TndeServerException(String message) {
        super(message);
    }

    public TndeServerException(String message, Throwable cause) {
        super(message, cause);
    }
}

