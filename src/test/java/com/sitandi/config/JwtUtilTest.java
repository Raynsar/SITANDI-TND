package com.sitandi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.jsonwebtoken.ExpiredJwtException;

/**
 * Unit test JwtUtil. Tanpa Test Double: kelas ini murni logika kriptografi tanpa dependency.
 */
class JwtUtilTest {

    private static final String SECRET = "rahasia-uji-minimal-32-karakter-aman!!";
    private static final long SATU_JAM = 3_600_000L;

    private JwtUtil util() {
        return new JwtUtil(SECRET, SATU_JAM);
    }

    // ================= constructor =================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("constructor: secret kosong -> IllegalStateException")
    void constructor_secretKosong(String secret) {
        assertThatThrownBy(() -> new JwtUtil(secret, SATU_JAM))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.secret belum diisi");
    }

    @Test
    @DisplayName("constructor: secret 31 byte (di bawah batas) -> IllegalStateException")
    void constructor_secretTerlaluPendek() {
        String duaPuluhSembilan = "a".repeat(31);

        assertThatThrownBy(() -> new JwtUtil(duaPuluhSembilan, SATU_JAM))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terlalu pendek");
    }

    @Test
    @DisplayName("constructor: secret tepat 32 byte diterima")
    void constructor_secretTepat32Byte() {
        JwtUtil util = new JwtUtil("a".repeat(32), SATU_JAM);

        assertThat(util.getExpirationMs()).isEqualTo(SATU_JAM);
    }

    // ================= generate & parse =================

    @Test
    @DisplayName("generateToken: username dan role dapat dibaca kembali")
    void generateToken_usernameDanRole() {
        JwtUtil util = util();

        String token = util.generateToken("adifa", "ADMIN");

        assertThat(util.extractUsername(token)).isEqualTo("adifa");
        assertThat(util.extractRole(token)).isEqualTo("ADMIN");
        assertThat(util.parse(token).getExpiration()).isAfter(util.parse(token).getIssuedAt());
    }

    // ================= isValid =================

    @Test
    @DisplayName("isValid: token sah -> true")
    void isValid_tokenSah() {
        JwtUtil util = util();

        assertThat(util.isValid(util.generateToken("adifa", "USER"))).isTrue();
    }

    @Test
    @DisplayName("isValid: token ditandatangani secret lain -> false")
    void isValid_secretBeda() {
        String tokenAsing = new JwtUtil("secret-lain-yang-panjangnya-32-karakter+", SATU_JAM)
                .generateToken("adifa", "USER");

        assertThat(util().isValid(tokenAsing)).isFalse();
    }

    @Test
    @DisplayName("isValid: token kedaluwarsa -> false, parse melempar ExpiredJwtException")
    void isValid_tokenKedaluwarsa() {
        JwtUtil kedaluwarsa = new JwtUtil(SECRET, -1000L);
        String token = kedaluwarsa.generateToken("adifa", "USER");

        assertThat(kedaluwarsa.isValid(token)).isFalse();
        assertThatThrownBy(() -> kedaluwarsa.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("isValid: token diubah isinya -> false")
    void isValid_tokenDimodifikasi() {
        JwtUtil util = util();
        String token = util.generateToken("adifa", "USER");
        String rusak = token.substring(0, token.length() - 2) + "xx";

        assertThat(util.isValid(rusak)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"bukan-jwt", "a.b.c"})
    @DisplayName("isValid: string sembarang, kosong, atau null -> false")
    void isValid_stringSembarang(String token) {
        assertThat(util().isValid(token)).isFalse();
    }
}
