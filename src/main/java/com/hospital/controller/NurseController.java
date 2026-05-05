package com.hospital.controller;

import com.hospital.security.UserPrincipal;
import com.hospital.service.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/nurse")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('NURSE','INDEPENDENT_NURSE')")
public class NurseController {

    private final NurseService nurseService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard(@AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.getDashboard(u.getId()));
    }

    @GetMapping("/patients")
    public ResponseEntity<?> patients(@RequestParam(defaultValue="0") int page,
                                      @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.getAssignedPatients(u.getId(), page));
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> tasks(@AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(nurseService.getTasks(u.getId()));
    }

    @PutMapping("/tasks/{id}/start")
    public ResponseEntity<?> startTask(@PathVariable Long id) {
        nurseService.startTask(id);
        return ResponseEntity.ok(Map.of("message", "Task started"));
    }

    @PutMapping("/tasks/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long id) {
        nurseService.completeTask(id);
        return ResponseEntity.ok(Map.of("message", "Task completed"));
    }

    // Legacy POST support
    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<?> completeTaskPost(@PathVariable Long id) {
        nurseService.completeTask(id);
        return ResponseEntity.ok(Map.of("message", "Task completed"));
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
