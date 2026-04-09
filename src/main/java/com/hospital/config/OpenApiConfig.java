package com.hospital.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger UI configuration.
 *
 * Swagger UI  →  http://localhost:8080/swagger-ui.html
 * API Docs    →  http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI hospitalOpenAPI() {
        return new OpenAPI()
            // ── Servers ───────────────────────────────────────────────────
            .servers(List.of(
                new Server().url("http://localhost:" + serverPort)
                            .description("Local Development Server"),
                new Server().url("https://api.yourhospital.com")
                            .description("Production Server")
            ))

            // ── Info ──────────────────────────────────────────────────────
            .info(new Info()
                .title("Hospital Management System — REST API")
                .description("""
                    ## Overview
                    Full-scale Hospital Chain Management System API.
                    
                    ### Authentication
                    All protected endpoints require a **Bearer JWT token**.
                    
                    1. Call `POST /api/auth/login` with your credentials.
                    2. Copy the `token` from the response.
                    3. Click **Authorize** above and enter: `Bearer <your-token>`
                    
                    ### Roles
                    | Role | Prefix |
                    |------|--------|
                    | ADMIN | `/api/admin/**` |
                    | DOCTOR | `/api/doctor/**` |
                    | NURSE | `/api/nurse/**` |
                    | PATIENT | `/api/patient/**` |
                    | RECEPTIONIST | `/api/receptionist/**` |
                    | PHARMACIST | `/api/pharmacy/**` |
                    | LAB_TECHNICIAN | `/api/lab/**` |
                    | BLOOD_BANK_MANAGER | `/api/blood-bank/**` |
                    | AMBULANCE_OPERATOR | `/api/ambulance/**` |
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("HMS Support")
                    .email("support@yourhospital.com")
                    .url("https://yourhospital.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT"))
            )

            // ── Global JWT Security Scheme ─────────────────────────────────
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter your JWT token. Example: **Bearer eyJhbGci...**")
                )
            );
    }
}
