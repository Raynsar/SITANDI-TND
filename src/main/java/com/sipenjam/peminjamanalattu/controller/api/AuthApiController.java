package com.sipenjam.peminjamanalattu.controller.api;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sipenjam.peminjamanalattu.config.JwtUtil;
import com.sipenjam.peminjamanalattu.dto.GantiPasswordRequestDto;
import com.sipenjam.peminjamanalattu.dto.LoginRequestDto;
import com.sipenjam.peminjamanalattu.dto.LoginResponseDto;
import com.sipenjam.peminjamanalattu.dto.PenggunaDto;
import com.sipenjam.peminjamanalattu.dto.RegisterRequestDto;
import com.sipenjam.peminjamanalattu.exception.AkunBelumAktifException;
import com.sipenjam.peminjamanalattu.model.Pengguna;
import com.sipenjam.peminjamanalattu.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoint akun untuk klien API (Postman/integrasi). Browser memakai form login di
 * /login yang menaruh JWT pada cookie; endpoint /login di sini mengembalikan token
 * mentah untuk dipakai lewat header Authorization.
 *
 * TODO(keamanan): /login dan /register belum punya rate limiting sehingga masih
 * terbuka terhadap brute force dan pendaftaran massal. Sebelum produksi, batasi
 * percobaan per IP/username (mis. bucket4j) atau pasang rate limit di reverse
 * proxy/WAF di depan Tomcat.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        // Kredensial salah -> AuthenticationException -> ditangani GlobalExceptionHandler
        // jadi 401 dengan pesan generik, tidak membedakan username vs password salah.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Status aktif dicek SETELAH password terbukti benar. Kalau dicek lebih awal
        // (lewat User.disabled()), pesan "menunggu persetujuan" akan bocor ke siapa pun
        // yang menebak username saja. Lihat AuthServiceImpl#loadUserByUsername.
        Pengguna pengguna = authService.getByUsername(authentication.getName());
        if (!pengguna.isAktif()) {
            throw new AkunBelumAktifException("Akun Anda belum disetujui admin, silakan tunggu");
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new IllegalStateException(
                        "Pengguna terautentikasi tanpa role: " + authentication.getName()));

        return ResponseEntity.ok(LoginResponseDto.builder()
                .token(jwtUtil.generateToken(authentication.getName(), role))
                .username(authentication.getName())
                .role(role)
                .expiresInMs(jwtUtil.getExpirationMs())
                .build());
    }

    /** Pendaftaran mandiri. Akun dibuat nonaktif sampai admin menyetujui. */
    @PostMapping("/register")
    public ResponseEntity<PenggunaDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /** Ganti password milik sendiri. Wajib login & tahu password lama. */
    @PostMapping("/ganti-password")
    public ResponseEntity<Void> gantiPassword(@Valid @RequestBody GantiPasswordRequestDto request,
                                              Principal principal) {
        authService.gantiPassword(request, principal.getName());
        // 204: berhasil, tidak ada isi. Token lama TETAP berlaku sampai kedaluwarsa —
        // ganti password tidak mencabut token yang sudah terbit (lihat JwtLogoutHandler).
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pengguna")
    public ResponseEntity<List<PenggunaDto>> daftarPengguna() {
        return ResponseEntity.ok(authService.daftarSemuaPengguna());
    }

    @PatchMapping("/pengguna/{id}/aktifkan")
    public ResponseEntity<PenggunaDto> aktifkan(@PathVariable Long id) {
        return ResponseEntity.ok(authService.aktifkan(id));
    }

    @PatchMapping("/pengguna/{id}/nonaktifkan")
    public ResponseEntity<PenggunaDto> nonaktifkan(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(authService.nonaktifkan(id, principal.getName()));
    }
}
