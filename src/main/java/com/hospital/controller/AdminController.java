package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.security.UserPrincipal;
import com.hospital.service.AdminService;
import com.hospital.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/doctors")
    public ResponseEntity<Page<Doctor>> doctors(@RequestParam(defaultValue="0") int page) {
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
                                             @RequestBody(required=false) Map<String,String> body,
                                             @AuthenticationPrincipal UserPrincipal me) {
        String reason = body != null ? body.getOrDefault("reason","") : "";
        adminService.rejectDoctor(id);
        auditService.log(me.getUsername(), "REJECT_DOCTOR", "Doctor", id, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nurses")
    public ResponseEntity<Page<Nurse>> nurses(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.getAllNurses(page));
    }

    @PostMapping("/nurses/{id}/approve")
    public ResponseEntity<Void> approveNurse(@PathVariable Long id,
                                             @AuthenticationPrincipal UserPrincipal me) {
        adminService.approveNurse(id);
        auditService.log(me.getUsername(), "APPROVE_NURSE", "Nurse", id, null);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/patients")
    public ResponseEntity<Page<Patient>> patients(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.getAllPatients(page));
    }

    @GetMapping("/patients/search")
    public ResponseEntity<Page<Patient>> searchPatients(@RequestParam String q,
                                                        @RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.searchPatients(q, page));
    }

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

    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> specializations() {
        return ResponseEntity.ok(adminService.getAllSpecializations());
    }

    @GetMapping("/branches")
    public ResponseEntity<List<HospitalBranch>> branches() {
        return ResponseEntity.ok(adminService.getAllBranches());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> users(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.getAllUsers(page));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> auditLogs(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(adminService.getAuditLogs(page));
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String,Object>> reports(@RequestParam String from,
                                                      @RequestParam String to) {
        return ResponseEntity.ok(adminService.getReports(from, to));
    }
}
