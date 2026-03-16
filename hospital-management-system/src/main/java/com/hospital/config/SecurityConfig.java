package com.hospital.config;

import com.hospital.security.CustomUserDetailsService;
import com.hospital.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/ws/**")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // ── Public pages ──────────────────────────
                .requestMatchers(
                    "/", "/login", "/register", "/register/**",
                    "/forgot-password", "/reset-password",
                    "/verify-email", "/error"
                ).permitAll()
                // ── Static resources ──────────────────────
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**",
                    "/webjars/**", "/favicon.ico"
                ).permitAll()
                // ── Public API ────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                // ── WebSocket ─────────────────────────────
                .requestMatchers("/ws/**").permitAll()

                // ── ADMIN ─────────────────────────────────
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ── DOCTOR ────────────────────────────────
                .requestMatchers("/doctor/**").hasRole("DOCTOR")

                // ── APPOINTMENTS (shared: patient books, doctor manages) ──
                .requestMatchers("/appointments/book/**").hasRole("PATIENT")
                .requestMatchers("/appointments/my/**").hasRole("PATIENT")
                .requestMatchers("/appointments/**").authenticated()

                // ── PATIENT ───────────────────────────────
                .requestMatchers("/patient/**").hasRole("PATIENT")

                // ── NURSE ─────────────────────────────────
                .requestMatchers("/nurse/**").hasAnyRole("NURSE", "INDEPENDENT_NURSE")

                // ── BLOOD BANK ────────────────────────────
                .requestMatchers("/blood-bank/**").hasRole("BLOOD_BANK_MANAGER")

                // ── AMBULANCE ─────────────────────────────
                .requestMatchers("/ambulance/**").hasRole("AMBULANCE_OPERATOR")

                // ── PHARMACY ──────────────────────────────
                .requestMatchers("/pharmacy/**").hasAnyRole("PHARMACIST", "DOCTOR", "NURSE")

                // ── LAB ───────────────────────────────────
                .requestMatchers("/lab/**").hasAnyRole("LAB_TECHNICIAN", "PHLEBOTOMIST", "DOCTOR")

                // ── EXTERNAL SERVICES ─────────────────────
                .requestMatchers("/medical-shop/**").hasRole("MEDICAL_SHOP_OWNER")
                .requestMatchers("/diagnostic/**").hasRole("DIAGNOSTIC_CENTER_OWNER")

                // ── BILLING ───────────────────────────────
                .requestMatchers("/billing/**").authenticated()

                // ── EVERYTHING ELSE ───────────────────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("jwt_token")
                .permitAll()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
