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
    private final JwtAuthenticationFilter  jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/ws/**"))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Public ─────────────────────────────────
                .requestMatchers(
                    "/", "/login", "/register", "/register/**",
                    "/forgot-password", "/reset-password",
                    "/verify-email", "/error"
                ).permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**",
                    "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()

                // ── ADMIN ──────────────────────────────────
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ── DOCTOR ─────────────────────────────────
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/lab/orders").hasAnyRole("DOCTOR","LAB_TECHNICIAN","PHLEBOTOMIST")

                // ── APPOINTMENTS ───────────────────────────
                .requestMatchers("/appointments/book/**", "/appointments/my/**",
                    "/appointments/*/cancel").hasRole("PATIENT")
                .requestMatchers("/appointments/**").authenticated()

                // ── PATIENT ────────────────────────────────
                .requestMatchers("/patient/**").hasRole("PATIENT")

                // ── NURSE ──────────────────────────────────
                .requestMatchers("/nurse/**").hasAnyRole("NURSE","INDEPENDENT_NURSE")

                // ── BLOOD BANK ─────────────────────────────
                .requestMatchers("/blood-bank/**")
                    .hasAnyRole("BLOOD_BANK_MANAGER","ADMIN")

                // ── AMBULANCE ──────────────────────────────
                .requestMatchers("/ambulance/**")
                    .hasAnyRole("AMBULANCE_OPERATOR","ADMIN")

                // ── PHARMACY ───────────────────────────────
                .requestMatchers("/pharmacy/**")
                    .hasAnyRole("PHARMACIST","DOCTOR","NURSE","ADMIN")

                // ── LAB ────────────────────────────────────
                .requestMatchers("/lab/**")
                    .hasAnyRole("LAB_TECHNICIAN","PHLEBOTOMIST","DOCTOR","ADMIN")

                // ── EXTERNAL SERVICES ──────────────────────
                .requestMatchers("/medical-shop/**").hasAnyRole("MEDICAL_SHOP_OWNER","ADMIN")
                .requestMatchers("/diagnostic/**").hasAnyRole("DIAGNOSTIC_CENTER_OWNER","ADMIN")

                // ── BILLING ────────────────────────────────
                .requestMatchers("/billing/**").authenticated()

                // ── DEFAULT ────────────────────────────────
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
