package com.sipenjam.peminjamanalattu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Konfigurasi MVC tambahan. Resource handler default Spring Boot sudah melayani
 * folder static/, jadi di sini hanya disiapkan sebagai titik ekstensi.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Sengaja kosong: mengandalkan konvensi default Spring Boot.
    // Tambahkan interceptor / formatter / view controller di sini bila diperlukan.
}
