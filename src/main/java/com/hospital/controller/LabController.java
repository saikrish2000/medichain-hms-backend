package com.hospital.controller;

import com.hospital.entity.LabTest;
import com.hospital.service.LabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/lab")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LAB_TECHNICIAN','PHLEBOTOMIST','ADMIN','DOCTOR')")
public class LabController {

    private final LabService labService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(labService.getDashboard());
    }

    @GetMapping("/tests")
    public ResponseEntity<?> tests() {
        return ResponseEntity.ok(labService.getAllTests());
    }

    @PostMapping("/tests")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','ADMIN')")
    public ResponseEntity<?> createTest(@RequestBody LabTest test) {
        return ResponseEntity.ok(labService.saveTest(test));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> orders(@RequestParam(defaultValue="0") int page,
                                    @RequestParam(required=false) String status) {
        return ResponseEntity.ok(labService.getOrders(page, status));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> order(@PathVariable Long id) {
        return ResponseEntity.ok(labService.getOrder(id));
    }

    @PostMapping("/orders/{id}/collect")
    public ResponseEntity<?> collect(@PathVariable Long id) {
        return ResponseEntity.ok(labService.markCollected(id));
    }

    @PostMapping("/orders/{id}/results")
    public ResponseEntity<?> uploadResults(@PathVariable Long id,
                                           @RequestBody Map<String,Object> body) {
        return ResponseEntity.ok(labService.uploadResults(id, body));
    }
}
