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
 * Security Configuration
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
        // Authentication Providers
        return new ProviderManager(List.of(tndeProvider, localProvider));
    }

    // CSRF Configuration
    private CookieCsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    // CSRF Request Handler
    private CsrfTokenRequestAttributeHandler csrfRequestHandler() {
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName(null);
        return handler;
    }

    // Security Context Repository
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

                .ignoringRequestMatchers("/api/v1/auth/login", "/api/v1/auth/register",
                        "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/reset-password").permitAll()
                .requestMatchers("/api/v1/auth/pengguna/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/pengguna/**").hasRole("ADMIN")
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
            .authenticationManager(authenticationManager)
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(csrfRequestHandler())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login", "/register",
                        "/forgot-password", "/reset-password",
                        "/css/**", "/js/**", "/images/**", "/error/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/organisasi/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")

                .successHandler(new JwtLoginSuccessHandler(jwtUtil, cookieService))
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler(new JwtLogoutHandler(cookieService))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(c -> c.securityContextRepository(securityContextRepository()))
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, cookieService, authService,
                            securityContextRepository()),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
