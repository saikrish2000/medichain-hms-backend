package com.hospital.config;

import com.hospital.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    private static final String[] SWAGGER_PATHS = {
        "/swagger-ui.html", "/swagger-ui/**",
        "/v3/api-docs", "/v3/api-docs/**",
        "/swagger-resources", "/swagger-resources/**", "/webjars/**"
    };

    private static final String[] WEBSOCKET_PATHS = {
        "/ws/**", "/ws/info/**", "/ws/info"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Public ────────────────────────────────────────────────
                .requestMatchers(SWAGGER_PATHS).permitAll()
                .requestMatchers(WEBSOCKET_PATHS).permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // ── Admin ─────────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── Doctor ────────────────────────────────────────────────
                .requestMatchers("/api/doctor/**").hasRole("DOCTOR")

                // ── Nurse ─────────────────────────────────────────────────
                .requestMatchers("/api/nurse/**").hasRole("NURSE")

                // ── Patient ───────────────────────────────────────────────
                .requestMatchers("/api/patient/**").hasRole("PATIENT")
                .requestMatchers("/api/billing/my-bills").hasRole("PATIENT")

                // ── Pharmacy ──────────────────────────────────────────────
                .requestMatchers("/api/pharmacy/**").hasAnyRole("PHARMACIST", "ADMIN")

                // ── Lab ───────────────────────────────────────────────────
                .requestMatchers("/api/lab/**").hasAnyRole("LAB_TECHNICIAN", "ADMIN", "DOCTOR")

                // ── Blood Bank ────────────────────────────────────────────
                .requestMatchers("/api/blood-bank/**").hasAnyRole("BLOOD_BANK_MANAGER", "ADMIN")

                // ── Ambulance + GPS ───────────────────────────────────────
                .requestMatchers("/api/ambulance/**").hasAnyRole("AMBULANCE_OPERATOR", "ADMIN")
                .requestMatchers("/api/ambulance/locations/all").hasAnyRole("ADMIN", "AMBULANCE_OPERATOR")

                // ── Payment ───────────────────────────────────────────────
                .requestMatchers("/api/payment/**").authenticated()

                // ── Billing ───────────────────────────────────────────────
                .requestMatchers("/api/billing/**").hasAnyRole("ADMIN", "RECEPTIONIST", "PATIENT")

                // ── Organ Donor ───────────────────────────────────────────
                .requestMatchers("/api/organ-donor/**").authenticated()

                // ── Receptionist ──────────────────────────────────────────
                .requestMatchers("/api/receptionist/**").hasAnyRole("RECEPTIONIST", "ADMIN")

                // ── Appointments ──────────────────────────────────────────
                .requestMatchers("/api/appointments/**").authenticated()

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
