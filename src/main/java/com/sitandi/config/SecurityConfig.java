package com.sitandi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitandi.service.AuthService;
import com.sitandi.model.Pengguna;

/**
 * Autentikasi aplikasi ini sepenuhnya berbasis JWT (stateless) — tidak ada HttpSession.
 *
 * Dua filter chain, dipisah karena cara menolak request-nya berbeda:
 *   @Order(1) apiFilterChain -> /api/**  : gagal auth dibalas 401 JSON.
 *   @Order(2) webFilterChain -> sisanya  : gagal auth diarahkan ke halaman /login.
 * Urutan penting: chain web memakai anyRequest() sehingga akan menelan /api/** bila
 * didahulukan.
 *
 * Token dibawa lewat cookie http-only untuk browser, atau header Bearer untuk klien
 * API (lihat JwtCookieService & JwtAuthenticationFilter).
 *
 * {@code @EnableMethodSecurity} mengaktifkan @PreAuthorize di service layer.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TndeAuthenticationProvider tndeAuthenticationProvider(AuthService authService) {
        return new TndeAuthenticationProvider(authService);
    }

    @Bean
    public AuthenticationManager authenticationManager(TndeAuthenticationProvider tndeProvider,
                                                       LocalUserDetailsService localUserDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider localProvider = new DaoAuthenticationProvider();
        localProvider.setUserDetailsService(localUserDetailsService);
        localProvider.setPasswordEncoder(passwordEncoder);
        // Prioritas: TNDE dulu, fallback ke lokal jika TNDE gagal (return null)
        return new ProviderManager(List.of(tndeProvider, localProvider));
    }

    /**
     * CSRF wajib aktif sejak token dipindah ke cookie.
     *
     * Cookie ikut terkirim otomatis oleh browser, jadi situs jahat bisa memancing
     * browser korban mengirim POST/PUT/PATCH ke aplikasi ini dengan kredensial korban
     * — itulah CSRF. Saat autentikasi masih memakai header Bearer yang dikirim manual
     * oleh JS, serangan ini tidak mungkin; sekarang mungkin, jadi proteksinya
     * dinyalakan. SameSite=Lax memang meredam sebagian besar kasus, tapi bukan
     * pertahanan yang lengkap (mis. serangan dari subdomain yang dikuasai penyerang).
     *
     * withHttpOnlyFalse() disengaja: token CSRF HARUS bisa dibaca JavaScript agar
     * api.js dapat mengirimkannya kembali lewat header X-XSRF-TOKEN. Ini aman dan
     * berbeda sifatnya dari token JWT — token CSRF bukan kredensial, ia hanya
     * membuktikan request berasal dari halaman kita sendiri.
     */
    private CookieCsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    /**
     * Handler CSRF, dengan dua penyimpangan dari default yang keduanya disengaja.
     *
     * 1. CsrfTokenRequestAttributeHandler (biasa), BUKAN Xor... yang jadi default
     *    Spring Security 6. Handler XOR menyamarkan token per-request, sedangkan
     *    CookieCsrfTokenRepository menyimpan nilai mentah di cookie. Kalau keduanya
     *    dipakai bersama, nilai mentah yang dibaca api.js dari cookie akan gagal
     *    divalidasi. Konsekuensinya kita kehilangan mitigasi BREACH bawaan handler XOR
     *    — dapat diterima di sini karena response tidak memuat rahasia lain.
     *
     * 2. setCsrfRequestAttributeName(null) mematikan pemuatan token yang ditunda.
     *    Secara default token baru dibuat saat ada yang membacanya, sehingga cookie
     *    XSRF-TOKEN hanya terbit di halaman yang memuat form (Thymeleaf membacanya saat
     *    merender th:action). Halaman seperti riwayat & katalog tidak punya form tapi
     *    tetap mengirim PATCH/POST lewat fetch — tanpa ini, cookie belum pernah terbit
     *    saat tombolnya ditekan dan request ditolak 403.
     */
    private CsrfTokenRequestAttributeHandler csrfRequestHandler() {
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName(null);
        return handler;
    }

    /**
     * Request ber-header Authorization dikecualikan dari CSRF.
     *
     * Alasannya: CSRF hanya relevan untuk kredensial yang dikirim browser secara
     * otomatis (cookie). Header Authorization harus dipasang eksplisit oleh klien,
     * dan situs lain tidak bisa memaksa browser menambahkannya, jadi klien API murni
     * (Postman/integrasi) tidak butuh token CSRF. Browser kita sendiri tidak pernah
     * mengirim header ini — ia memakai cookie — sehingga tetap terlindungi.
     */
    /**
     * Penyimpan SecurityContext: request attribute, BUKAN session dan bukan Null.
     *
     * Ini yang membuat aplikasi tetap stateless — context hanya hidup selama satu
     * request, tidak pernah menyentuh HttpSession.
     *
     * NullSecurityContextRepository sempat dipakai di sini dan ternyata SALAH:
     * {@code containsContext()} miliknya selalu mengembalikan false, sehingga
     * SessionManagementFilter menyangka setiap request adalah autentikasi baru lalu
     * menjalankan CsrfAuthenticationStrategy — token CSRF ikut dirotasi di SETIAP
     * request, membuat nilai yang dibaca api.js gampang basi dan request paralel saling
     * menimpa. Dengan RequestAttributeSecurityContextRepository, JwtAuthenticationFilter
     * memanggil saveContext() sehingga containsContext() bernilai true dan filter itu
     * tidak lagi salah kira. Rotasi token CSRF tetap terjadi — tapi hanya saat login,
     * sebagaimana mestinya.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new RequestAttributeSecurityContextRepository();
    }

    private RequestMatcher klienBearer() {
        return request -> {
            String header = request.getHeader("Authorization");
            return header != null && header.startsWith("Bearer ");
        };
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtUtil jwtUtil,
                                              JwtCookieService cookieService,
                                              AuthService authService,
                                              ObjectMapper objectMapper) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(csrfRequestHandler())
                .ignoringRequestMatchers(klienBearer())
                // Login & register belum punya identitas apa pun untuk dicuri, dan
                // keduanya dipanggil sebelum token CSRF sempat terbit.
                .ignoringRequestMatchers("/api/v1/auth/login", "/api/v1/auth/register")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                // Kelola akun: khusus ADMIN.
                .requestMatchers("/api/v1/auth/pengguna/**").hasRole("ADMIN")
                // Struktur organisasi: read-only, bisa diakses semua role yang sudah login
                .requestMatchers(HttpMethod.GET, "/api/v1/organisasi/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(c -> c.securityContextRepository(securityContextRepository()))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    new JwtAuthenticationEntryPoint(objectMapper)))
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, cookieService, authService,
                            securityContextRepository()),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http,
                                              JwtUtil jwtUtil,
                                              JwtCookieService cookieService,
                                              AuthService authService,
                                              AuthenticationFailureHandler failureHandler,
                                              AuthenticationManager authenticationManager) throws Exception {
        http
            .authenticationManager(authenticationManager) // EKSPLISIT: pakai bean kita (TNDE + Dao)
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(csrfRequestHandler())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login", "/register",
                        "/css/**", "/js/**", "/images/**", "/error/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Halaman struktur organisasi: bisa diakses semua role yang sudah login
                .requestMatchers("/organisasi/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                // Menerbitkan JWT + menaruhnya di cookie, lalu redirect sesuai role.
                .successHandler(new JwtLoginSuccessHandler(jwtUtil, cookieService, authService))
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler(new JwtLogoutHandler(cookieService))
                .logoutSuccessUrl("/login?logout")
                // Tidak ada session untuk di-invalidate; JSESSIONID pun tidak pernah
                // dibuat lagi. Yang perlu dibuang hanya cookie JWT di atas.
                .permitAll()
            )
            // Stateless: SecurityContext TIDAK disimpan antar request. Identitas
            // dibangun ulang dari cookie JWT oleh JwtAuthenticationFilter setiap kali.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(c -> c.securityContextRepository(securityContextRepository()))
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, cookieService, authService,
                            securityContextRepository()),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
