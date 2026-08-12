package com.sitandi.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception ketika login ke API eksternal TNDE gagal.
 *
 * EXTENDS AuthenticationException (bukan RuntimeException biasa) agar konsisten
 * dengan hierarki exception Spring Security. Meski begitu, TndeAuthenticationProvider
 * TIDAK boleh melempar exception ini ke ProviderManager — ia harus return null
 * agar fallback ke DaoAuthenticationProvider berjalan.
 */
public class TndeLoginException extends AuthenticationException {

    public TndeLoginException(String message) {
        super(message);
    }

    public TndeLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}

