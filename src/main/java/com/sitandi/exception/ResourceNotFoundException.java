package com.sitandi.exception;

/**
 * Dilempar saat entity yang diminta tidak ditemukan. Dipetakan ke HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " dengan id " + id + " tidak ditemukan");
    }
}
