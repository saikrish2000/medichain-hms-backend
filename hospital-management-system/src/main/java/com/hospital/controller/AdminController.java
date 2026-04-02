package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.service.AdminService;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ── Doctors ──────────────────────────────────────────
    @GetMapping("/doctors/pending")
    public ResponseEntity<?> pendingDoctors(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.getPendingDoctors(page));
    }

    @GetMapping("/doctors")
    public ResponseEntity<?> allDoctors(
            @RequestParam(required=false) String status,
            @RequestParam(defaultValue="0") int page) {
        Doctor.ApprovalStatus s = status!=null ? Doctor.ApprovalStatus.valueOf(status) : null;
        return ResponseEntity.ok(adminService.getAllDoctors(s, page));
    }

    @PostMapping("/doctors/{id}/approve")
    public ResponseEntity<?> approveDoctor(@PathVariable Long id,
                                            @AuthenticationPrincipal UserPrincipal admin) {
        adminService.approveDoctor(id, admin);
        return ResponseEntity.ok(Map.of("message","Doctor approved"));
    }

    @PostMapping("/doctors/{id}/reject")
    public ResponseEntity<?> rejectDoctor(@PathVariable Long id,
                                           @RequestBody Map<String,String> body,
                                           @AuthenticationPrincipal UserPrincipal admin) {
        adminService.rejectDoctor(id, body.get("reason"), admin);
        return ResponseEntity.ok(Map.of("message","Doctor rejected"));
    }

    @PostMapping("/doctors/{id}/suspend")
    public ResponseEntity<?> suspendDoctor(@PathVariable Long id,
                                            @RequestBody Map<String,String> body,
                                            @AuthenticationPrincipal UserPrincipal admin) {
        adminService.suspendDoctor(id, body.get("reason"), admin);
        return ResponseEntity.ok(Map.of("message","Doctor suspended"));
    }

    // ── Nurses ───────────────────────────────────────────
    @GetMapping("/nurses/pending")
    public ResponseEntity<?> pendingNurses(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.getPendingNurses(page));
    }

    @PostMapping("/nurses/{id}/approve")
    public ResponseEntity<?> approveNurse(@PathVariable Long id,
                                           @AuthenticationPrincipal UserPrincipal admin) {
        adminService.approveNurse(id, admin);
        return ResponseEntity.ok(Map.of("message","Nurse approved"));
    }

    @PostMapping("/nurses/{id}/reject")
    public ResponseEntity<?> rejectNurse(@PathVariable Long id,
                                          @RequestBody Map<String,String> body,
                                          @AuthenticationPrincipal UserPrincipal admin) {
        adminService.rejectNurse(id, body.get("reason"), admin);
        return ResponseEntity.ok(Map.of("message","Nurse rejected"));
    }

    // ── Departments ──────────────────────────────────────
    @GetMapping("/departments")
    public ResponseEntity<?> departments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody Department dept) {
        return ResponseEntity.ok(adminService.createDepartment(dept));
    }

    // ── Specializations ──────────────────────────────────
    @GetMapping("/specializations")
    public ResponseEntity<?> specializations() {
        return ResponseEntity.ok(adminService.getAllSpecializations());
    }

    @PostMapping("/specializations")
    public ResponseEntity<?> createSpec(@RequestBody Specialization spec) {
        return ResponseEntity.ok(adminService.createSpecialization(spec));
    }

    // ── Branches ─────────────────────────────────────────
    @GetMapping("/branches")
    public ResponseEntity<?> branches() {
        return ResponseEntity.ok(adminService.getAllBranches());
    }

    @PostMapping("/branches")
    public ResponseEntity<?> createBranch(@RequestBody HospitalBranch branch) {
        return ResponseEntity.ok(adminService.createBranch(branch));
    }

    // ── Users ─────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> users(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.getAllUsers(page));
    }

    @PostMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUser(@PathVariable Long id,
                                         @AuthenticationPrincipal UserPrincipal admin) {
        adminService.toggleUserStatus(id, admin);
        return ResponseEntity.ok(Map.of("message","User status toggled"));
    }

    // ── Patients ─────────────────────────────────────────
    @GetMapping("/patients")
    public ResponseEntity<?> patients(
            @RequestParam(required=false) String q,
            @RequestParam(defaultValue="0") int page) {
        if (q!=null && !q.isBlank()) return ResponseEntity.ok(adminService.searchPatients(q, page));
        return ResponseEntity.ok(adminService.getAllPatients(page));
    }

    // ── Audit & Reports ──────────────────────────────────
    @GetMapping("/audit-logs")
    public ResponseEntity<?> auditLogs(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.getAuditLogs(page));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> reports(
            @RequestParam(required=false) String from,
            @RequestParam(required=false) String to) {
        return ResponseEntity.ok(adminService.getReports(from, to));
    }
}
