package com.hospital.controller;

import com.hospital.entity.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/nurse")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('NURSE','INDEPENDENT_NURSE')")
public class NurseController {

    private final NurseService nurseService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard(
            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.getDashboard(u.getId()));
    }

    @GetMapping("/patients")
    public ResponseEntity<?> patients(
            @RequestParam(defaultValue="0") int page,
            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.getAssignedPatients(u.getId(), page));
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> tasks(@AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.getTasks(u.getId()));
    }

    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long id) {
        nurseService.completeTask(id);
        return ResponseEntity.ok(Map.of("message","Task completed"));
    }

    @PostMapping("/vitals")
    public ResponseEntity<?> recordVitals(@RequestBody Map<String,Object> body,
                                           @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.recordVitals(body, u.getId()));
    }

    @GetMapping("/handover")
    public ResponseEntity<?> handover(@AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.getHandoverNotes(u.getId()));
    }

    @PostMapping("/handover")
    public ResponseEntity<?> createHandover(@RequestBody Map<String,Object> body,
                                             @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.createHandoverNote(body, u.getId()));
    }

    @GetMapping("/emar")
    public ResponseEntity<?> emar(@AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.getEmarRecords(u.getId()));
    }

    @PostMapping("/emar/administer")
    public ResponseEntity<?> administerMed(@RequestBody Map<String,Object> body,
                                            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.administerMedication(body, u.getId()));
    }
}
