package com.hospital.controller;

import com.hospital.entity.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.service.AuditService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ── Doctors ──────────────────────────────────────────────────────────
    @GetMapping("/doctors")
    public ResponseEntity<Page<Doctor>> doctors(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(adminService.getAllDoctors(page));
    }

    @PostMapping("/doctors/{id}/approve")
    public ResponseEntity<Void> approveDoctor(@PathVariable Long id,
                                               @AuthenticationPrincipal UserPrincipal me) {
        adminService.approveDoctor(id);
        auditService.log(me.getUsername(), "APPROVE_DOCTOR", "Doctor", id, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/doctors/{id}/reject")
    public ResponseEntity<Void> rejectDoctor(@PathVariable Long id,
                                              @AuthenticationPrincipal UserPrincipal me) {
        adminService.rejectDoctor(id);
        auditService.log(me.getUsername(), "REJECT_DOCTOR", "Doctor", id, null);
        return ResponseEntity.ok().build();
    }

    // ── Patients ─────────────────────────────────────────────────────────
    @GetMapping("/patients")
    public ResponseEntity<Page<Patient>> patients(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(adminService.getAllPatients(page));
    }

    @GetMapping("/patients/search")
    public ResponseEntity<Page<Patient>> searchPatients(@RequestParam String q,
                                                         @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(adminService.searchPatients(q, page));
    }

    // ── Departments ───────────────────────────────────────────────────────
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> departments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department dept) {
        return ResponseEntity.ok(adminService.createDepartment(dept));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        adminService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Specializations ───────────────────────────────────────────────────
    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> specializations() {
        return ResponseEntity.ok(adminService.getAllSpecializations());
    }

    // ── Branches ──────────────────────────────────────────────────────────
    @GetMapping("/branches")
    public ResponseEntity<List<HospitalBranch>> branches() {
        return ResponseEntity.ok(adminService.getAllBranches());
    }

    // ── Users ─────────────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<Page<User>> users(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(adminService.getAllUsers(page));
    }

    // ── Audit Logs ────────────────────────────────────────────────────────
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> auditLogs(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(adminService.getAuditLogs(page));
    }

    // ── Reports ───────────────────────────────────────────────────────────
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> reports(@RequestParam String from,
                                                        @RequestParam String to) {
        return ResponseEntity.ok(adminService.getReports(from, to));
    }
}
