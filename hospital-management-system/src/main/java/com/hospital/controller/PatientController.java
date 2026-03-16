package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository       patientRepo;
    private final AppointmentService      appointmentService;
    private final MedicalRecordService    recordService;
    private final DepartmentRepository    deptRepo;

    // ── DASHBOARD ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Patient patient = patientRepo.findByUserId(user.getId()).orElse(null);
        if (patient == null) return "redirect:/patient/complete-profile";

        model.addAttribute("patient",      patient);
        model.addAttribute("appointments", appointmentService.getPatientAppointments(patient.getId(), 0));
        model.addAttribute("recentRecords", recordService.getPatientRecords(patient.getId(), 0).getContent());
        return "patient/dashboard";
    }

    // ── COMPLETE PROFILE ───────────────────────────────────
    @GetMapping("/complete-profile")
    public String completeProfile(@AuthenticationPrincipal UserPrincipal user, Model model) {
        model.addAttribute("patient", new Patient());
        return "patient/complete-profile";
    }

    @PostMapping("/complete-profile")
    public String saveProfile(@AuthenticationPrincipal UserPrincipal user,
                              @ModelAttribute Patient form,
                              RedirectAttributes ra) {
        // Profile completion handled in AuthService; placeholder
        ra.addFlashAttribute("success", "Profile completed!");
        return "redirect:/patient/dashboard";
    }

    // ── PROFILE ────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Patient patient = patientRepo.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("patient", patient);
        return "patient/profile";
    }

    // ── MEDICAL RECORDS ────────────────────────────────────
    @GetMapping("/records")
    public String records(@AuthenticationPrincipal UserPrincipal user,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        Patient patient = patientRepo.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("records", recordService.getPatientRecords(patient.getId(), page));
        model.addAttribute("patient", patient);
        return "patient/records";
    }

    @GetMapping("/records/{id}")
    public String recordDetail(@PathVariable Long id, Model model) {
        model.addAttribute("record", recordService.getRecord(id));
        return "patient/record-detail";
    }
}
