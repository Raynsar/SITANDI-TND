package com.sitandi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Render landing page.
 *
 * <p>Halaman ini dulunya static/index.html (welcome page bawaan Spring Boot), tapi
 * dipindah ke templates/index.html supaya link & aset-nya bisa pakai `@{...}` yang
 * sadar context path — file di static/ tidak diproses Thymeleaf, jadi path-nya
 * terpaksa hardcoded '/...' dan bakal 404 saat WAR di-deploy ke context non-root.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
