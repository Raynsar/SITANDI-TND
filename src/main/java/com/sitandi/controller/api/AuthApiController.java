package com.sitandi.controller.api;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sitandi.config.JwtUtil;
import com.sitandi.dto.GantiPasswordRequestDto;
import com.sitandi.dto.KelolaPenggunaResponseDto;
import com.sitandi.dto.LoginRequestDto;
import com.sitandi.dto.LoginResponseDto;
import com.sitandi.dto.PenggunaDto;
import com.sitandi.dto.RegisterRequestDto;
import com.sitandi.dto.TndeLoginResponseDto;
import com.sitandi.exception.AkunBelumAktifException;
import com.sitandi.model.Pengguna;
import com.sitandi.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        TndeLoginResponseDto tndeResponse = authService.loginTnde(request.getUsername(), request.getPassword());
        Pengguna pengguna = authService.getByUsername(tndeResponse.getData().getUser().getUsername());
        if (!pengguna.isAktif()) {
            throw new AkunBelumAktifException("Akun Anda belum disetujui admin, silakan tunggu");
        }

        String role = "ROLE_" + pengguna.getRole().name();

        return ResponseEntity.ok(LoginResponseDto.builder()
                .token(jwtUtil.generateToken(pengguna.getUsername(), role))
                .username(pengguna.getUsername())
                .role(role)
                .expiresInMs(jwtUtil.getExpirationMs())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<PenggunaDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/ganti-password")
    public ResponseEntity<Void> gantiPassword(@Valid @RequestBody GantiPasswordRequestDto request,
                                              Principal principal) {
        authService.gantiPassword(request, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pengguna")
    public ResponseEntity<KelolaPenggunaResponseDto> daftarPengguna() {
        return ResponseEntity.ok(KelolaPenggunaResponseDto.builder()
                .daftarPengguna(authService.daftarSemuaPengguna())
                .jumlahMenungguAktivasi(authService.daftarMenungguAktivasi().size())
                .build());
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
