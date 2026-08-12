package com.sitandi.config;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sitandi.model.Pengguna;
import com.sitandi.service.AuthService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Satu-satunya mekanisme autentikasi aplikasi ini: memvalidasi JWT dan mengisi
 * SecurityContext. Tidak ada HttpSession — token diverifikasi ulang setiap request.
 *
 * Token dicari dari dua tempat, sesuai jenis kliennya:
 *   1. Header "Authorization: Bearer <token>" — klien API (Postman, integrasi).
 *   2. Cookie http-only SITANDI_TOKEN — browser, karena navigasi halaman tidak
 *      bisa menyisipkan header.
 * Header diprioritaskan agar klien API yang memakai browser yang sama (mis. sedang
 * login di tab lain) tetap memakai identitas tokennya, bukan cookie-nya.
 *
 * Filter ini TIDAK menolak request tanpa token; ia hanya membiarkan SecurityContext
 * kosong dan menyerahkan keputusan ke authorizeHttpRequests. Itu penting supaya
 * halaman publik (/login, /register) tetap bisa diakses, dan supaya penolakan
 * ditangani entry point yang sesuai chain-nya — 401 JSON untuk API, redirect ke
 * /login untuk halaman.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final JwtCookieService cookieService;
    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtCookieService cookieService,
                                   AuthService authService,
                                   SecurityContextRepository securityContextRepository) {
        this.jwtUtil = jwtUtil;
        this.cookieService = cookieService;
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = ambilToken(request);

        if (token.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
            autentikasi(token.get(), request, response);
        }

        filterChain.doFilter(request, response);
    }

    private void autentikasi(String token, HttpServletRequest request, HttpServletResponse response) {
        try {
            Claims claims = jwtUtil.parse(token);
            String username = claims.getSubject();
            if (!StringUtils.hasText(username)) {
                throw new JwtException("Token tanpa subject");
            }

            String role = jwtUtil.extractRole(token);
            if (!StringUtils.hasText(role)) {
                throw new JwtException("Token tanpa role");
            }

            // Kalau user terdaftar lokal, cek aktifitasnya; kalau tidak, teruskan sebagai
            // pengguna eksternal yang diautentikasi oleh TNDE.
            boolean active = true;
            try {
                Pengguna pengguna = authService.getByUsername(username);
                if (!pengguna.isAktif()) {
                    active = false;
                }
            } catch (UsernameNotFoundException ex) {
                active = true;
            }

            if (!active) {
                tolak(response, request);
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null,
                    List.of(new SimpleGrantedAuthority(role)));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // saveContext() WAJIB, bukan sekadar mengisi SecurityContextHolder.
            // Repository-nya RequestAttributeSecurityContextRepository, jadi ini hanya
            // menaruh context pada atribut request (tetap stateless, tidak ada session).
            // Tanpa ini, containsContext() bernilai false dan SessionManagementFilter
            // mengira tiap request adalah login baru, lalu merotasi token CSRF terus-
            // menerus sampai cookie XSRF-TOKEN yang dipegang api.js jadi basi.
            securityContextRepository.saveContext(context, request, response);

        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            // Token kedaluwarsa, signature salah, formatnya rusak, atau akunnya sudah
            // dihapus dari DB. Perlakukan sebagai anonim, bukan error: biarkan
            // authorizeHttpRequests yang memutuskan 401/redirect sesuai chain.
            tolak(response, request);
        }
    }

    private void tolak(HttpServletResponse response, HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        cookieService.hapus(response, request.getContextPath());
    }

    private Optional<String> ambilToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            String nilai = header.substring(BEARER_PREFIX.length()).trim();
            return nilai.isEmpty() ? Optional.empty() : Optional.of(nilai);
        }
        return cookieService.baca(request);
    }
}
