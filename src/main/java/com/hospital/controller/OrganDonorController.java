package com.hospital.controller;

import com.hospital.security.UserPrincipal;
import com.hospital.service.OrganDonorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/organ-donor")
@RequiredArgsConstructor
@Tag(name = "Organ Donor", description = "Organ donor registry")
public class OrganDonorController {

    private final OrganDonorService organDonorService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() { return ResponseEntity.ok(organDonorService.getDashboardStats()); }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,Object> body, @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(organDonorService.registerDonor(u.getId(), body));
    }

    @GetMapping("/donors")
    public ResponseEntity<?> donors(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(organDonorService.getAllDonors(page));
    }

    @GetMapping("/donors/{id}")
    public ResponseEntity<?> donor(@PathVariable Long id) { return ResponseEntity.ok(organDonorService.getDonorById(id)); }

    @PostMapping("/requests")
    public ResponseEntity<?> createRequest(@RequestBody Map<String,Object> body) {
        return ResponseEntity.ok(organDonorService.createRequest(body));
    }

    @GetMapping("/requests")
    public ResponseEntity<?> requests(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(organDonorService.getAllRequests(page));
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String,String> body) {
        return ResponseEntity.ok(organDonorService.updateRequestStatus(id, body.get("status")));
    }
}
