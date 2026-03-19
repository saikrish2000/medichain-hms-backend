package com.hospital.controller;

import com.hospital.entity.AuditLog;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAdvancedController {

    private final AuditLogRepository    auditLogRepo;
    private final UserRepository        userRepo;
    private final PatientRepository     patientRepo;

    // ── AUDIT LOGS ─────────────────────────────────────────
    @GetMapping("/audit-logs")
    public String auditLogs(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "") String search,
                             @RequestParam(defaultValue = "") String action,
                             Model model) {
        Page<AuditLog> logs = auditLogRepo.findAll(
            PageRequest.of(page, 20, Sort.by("createdAt").descending()));
        model.addAttribute("auditLogs",    logs.getContent());
        model.addAttribute("totalLogs",    auditLogRepo.count());
        model.addAttribute("activeUsers",  userRepo.count());
        model.addAttribute("loginCount",   0L); // Placeholder
        model.addAttribute("suspiciousCount", 3L);
        return "admin/audit-logs";
    }

    // ── DIGITAL TWIN ───────────────────────────────────────
    @GetMapping("/digital-twin")
    public String digitalTwin(Model model) {
        return "admin/digital-twin";
    }

    // ── WORKFORCE AI ───────────────────────────────────────
    @GetMapping("/workforce")
    public String workforce(Model model) {
        return "admin/workforce";
    }

    // ── REPORTS ────────────────────────────────────────────
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("totalPatients", patientRepo.count());
        return "admin/reports";
    }

    // ── INVENTORY ──────────────────────────────────────────
    @GetMapping("/inventory")
    public String inventory(Model model) {
        return "admin/inventory";
    }

    // ── BILLING ────────────────────────────────────────────
    @GetMapping("/billing")
    public String billing(Model model) {
        return "admin/billing";
    }

    // ── PATIENTS LIST ──────────────────────────────────────
    @GetMapping("/patients")
    public String patients(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "") String q,
                            Model model) {
        if (q.isBlank()) {
            model.addAttribute("patients", patientRepo.findAll(
                PageRequest.of(page, 20, Sort.by("createdAt").descending())));
        } else {
            model.addAttribute("patients", patientRepo.search(q,
                PageRequest.of(page, 20)));
        }
        model.addAttribute("q", q);
        return "admin/patients";
    }
}
