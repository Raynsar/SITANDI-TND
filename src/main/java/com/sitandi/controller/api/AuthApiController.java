package com.sitandi.controller.api;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sitandi.config.JwtCookieService;

import com.sitandi.config.JwtUtil;
import com.sitandi.dto.ForgotPasswordRequestDto;
import com.sitandi.dto.GantiPasswordRequestDto;
import com.sitandi.dto.KelolaPenggunaResponseDto;
import com.sitandi.dto.LoginRequestDto;
import com.sitandi.dto.LoginResponseDto;
import com.sitandi.dto.PenggunaDto;
import com.sitandi.dto.RegisterRequestDto;
import com.sitandi.dto.ResetPasswordRequestDto;
import com.sitandi.dto.TndeLoginResponseDto;
import com.sitandi.exception.AkunBelumAktifException;
import com.sitandi.model.Pengguna;
import com.sitandi.service.AuthService;
import com.sitandi.service.PasswordResetService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final com.sitandi.service.ActivityLogService activityLogService;
    private final JwtUtil jwtUtil;
    private final JwtCookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        TndeLoginResponseDto tndeResponse;
        try {
            tndeResponse = authService.loginTnde(request.getUsername(), request.getPassword());
        } catch (RuntimeException e) {
            activityLogService.log(null, "LOGIN_GAGAL",
                    "Login API gagal untuk username '" + request.getUsername() + "'");
            throw e;
        }
        Pengguna pengguna = authService.getByUsername(tndeResponse.getData().getUser().getUsername());
        if (!pengguna.isAktif()) {
            throw new AkunBelumAktifException("Akun Anda belum disetujui admin, silakan tunggu");
        }
        activityLogService.log(pengguna, "LOGIN_BERHASIL", "Login berhasil via API");

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
                                              Principal principal,
                                              jakarta.servlet.http.HttpServletRequest httpRequest,
                                              HttpServletResponse response) {
        authService.gantiPassword(request, principal.getName());
        cookieService.hapus(response, httpRequest.getContextPath());
        try {
            activityLogService.log(authService.getByUsername(principal.getName()),
                    "GANTI_PASSWORD", "Pengguna mengganti password");
        } catch (Exception ignored) { }
        return ResponseEntity.noContent().build();
    }

    // ===== Lupa / Reset Password (public — tanpa autentikasi) =====

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message",
                "Link reset password telah dikirim. Silakan cek inbox Anda."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {
        if (!request.getPasswordBaru().equals(request.getKonfirmasiPasswordBaru())) {
            throw new IllegalArgumentException("Konfirmasi password baru tidak cocok");
        }
        passwordResetService.resetPassword(request.getToken(), request.getPasswordBaru());
        activityLogService.log(null, "RESET_PASSWORD", "Reset password via token email");
        return ResponseEntity.ok(Map.of("message", "Password berhasil direset. Silakan login dengan password baru."));
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

    @DeleteMapping("/pengguna/{id}")
    public ResponseEntity<Void> hapusPengguna(@PathVariable Long id, Principal principal) {
        authService.hapusPengguna(id, principal.getName());
        try {
            activityLogService.log(authService.getByUsername(principal.getName()),
                    "HAPUS_USER", "Menghapus akun pengguna id=" + id);
        } catch (Exception ignored) { }
        return ResponseEntity.noContent().build();
    }
}
