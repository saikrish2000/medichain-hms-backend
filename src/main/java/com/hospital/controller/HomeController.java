package com.hospital.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Tag(name = "Health Check", description = "Server health and welcome endpoints")
@RestController
public class HomeController {
    @GetMapping("/")
    public ResponseEntity<?> root() {
        return ResponseEntity.ok(Map.of(
            "app","MediChain HMS API",
            "version","2.0.0",
            "status","running",
            "docs","/api-docs"
        ));
    }

    @GetMapping("/api/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status","UP","service","MediChain HMS"));
    }
}
