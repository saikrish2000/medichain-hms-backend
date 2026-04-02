package com.hospital.controller;

import com.hospital.entity.LabTest;
import com.hospital.service.LabService;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    @GetMapping("/tests")
    public ResponseEntity<?> tests() {
        return ResponseEntity.ok(labService.getAllTests());
    }

    @PostMapping("/tests")
    public ResponseEntity<?> saveTest(@RequestBody LabTest test) {
        return ResponseEntity.ok(labService.saveTest(test));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> orders(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(labService.getAllOrders(page));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> order(@PathVariable Long id) {
        return ResponseEntity.ok(labService.getOrderById(id));
    }

    @PostMapping("/orders/{id}/collect")
    public ResponseEntity<?> collect(@PathVariable Long id,
                                      @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(labService.collectSample(id, u));
    }

    @PostMapping("/orders/{id}/results")
    public ResponseEntity<?> enterResults(@PathVariable Long id,
                                           @RequestBody Map<String,Object> body,
                                           @AuthenticationPrincipal UserPrincipal u) {
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> results = (List<Map<String,Object>>) body.get("results");
        String notes = (String) body.getOrDefault("notes","");
        return ResponseEntity.ok(labService.enterResults(id, results, notes, u));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(labService.getDashboardStats());
    }
}
