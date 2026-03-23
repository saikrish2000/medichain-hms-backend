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

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository    patientRepo;
    private final AppointmentService   appointmentService;
    private final MedicalRecordService recordService;

    // ── DASHBOARD ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Patient patient = patientRepo.findByUserId(user.getId()).orElse(null);
        if (patient == null) return "redirect:/patient/complete-profile";

        var appts        = appointmentService.getPatientAppointments(patient.getId(), 0);
        var recentRecords= recordService.getPatientRecords(patient.getId(), 0).getContent();
        var nextAppt     = appointmentService.getNextAppointment(patient.getId());

        model.addAttribute("patient",       patient);
        model.addAttribute("appointments",  appts);
        model.addAttribute("recentRecords", recentRecords);
        model.addAttribute("nextAppointment", nextAppt);
        return "patient/dashboard";
    }

    // ── COMPLETE PROFILE ───────────────────────────────────
    @GetMapping("/complete-profile")
    public String completeProfile(Model model) {
        model.addAttribute("patient", new Patient());
        return "patient/complete-profile";
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
                          @RequestParam(defaultValue = "0") int page, Model model) {
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

    // ── PLACEHOLDER ROUTES ─────────────────────────────────
    @GetMapping("/vitals")
    public String vitals(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Patient patient = patientRepo.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("records", recordService.getPatientRecords(patient.getId(), 0).getContent());
        return "patient/vitals";
    }

    @GetMapping("/labs")
    public String labs(Model model) { return "patient/labs"; }

    @GetMapping("/adherence")
    public String adherence(Model model) { return "patient/adherence"; }

    @GetMapping("/wearables")
    public String wearables(Model model) { return "patient/wearables"; }
}
