package com.sitandi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point aplikasi.
 *
 * <p>Mewarisi {@link SpringBootServletInitializer} supaya WAR hasil `mvn clean package`
 * bisa di-bootstrap oleh Tomcat eksternal (Tomcat memanggil {@link #configure}).
 * Method {@link #main} tetap dipertahankan agar development lokal via
 * `mvn spring-boot:run` (embedded Tomcat) tetap bisa jalan.
 */
@SpringBootApplication
public class SitandiApplication extends SpringBootServletInitializer {

    /** Dipakai Tomcat eksternal saat WAR di-deploy ke folder webapps/. */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(SitandiApplication.class);
    }

    /** Dipakai saat dijalankan lokal (embedded Tomcat). */
    public static void main(String[] args) {
        SpringApplication.run(SitandiApplication.class, args);
    }
}
