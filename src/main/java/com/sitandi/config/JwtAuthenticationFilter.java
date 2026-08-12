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
 * JWT Authentication Filter
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

            // Check User Active Status
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

            // Save Context to Repository
            securityContextRepository.saveContext(context, request, response);

        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            // Invalid Token Handling
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
