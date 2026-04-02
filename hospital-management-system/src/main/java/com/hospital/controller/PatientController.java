package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.service.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final PatientService      patientService;
    private final AppointmentService  appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final BillingService      billingService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard(
            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(patientService.getDashboard(u.getId()));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(patientService.getProfile(u.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Patient data,
                                            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(patientService.updateProfile(u.getId(), data));
    }

    // ── Appointments ─────────────────────────────────────
    @GetMapping("/appointments")
    public ResponseEntity<?> appointments(
            @RequestParam(defaultValue="0") int page,
            @AuthenticationPrincipal UserPrincipal u) {
        Long pid = patientService.getPatientIdByUserId(u.getId());
        return ResponseEntity.ok(appointmentService.getPatientAppointments(pid, page));
    }

    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id,
                                     @AuthenticationPrincipal UserPrincipal u) {
        appointmentService.cancelByPatient(id, u.getId());
        return ResponseEntity.ok(Map.of("message","Appointment cancelled"));
    }

    // ── Medical Records ──────────────────────────────────
    @GetMapping("/records")
    public ResponseEntity<?> records(
            @RequestParam(defaultValue="0") int page,
            @AuthenticationPrincipal UserPrincipal u) {
        Long pid = patientService.getPatientIdByUserId(u.getId());
        return ResponseEntity.ok(medicalRecordService.getPatientRecords(pid, page));
    }

    @GetMapping("/vitals")
    public ResponseEntity<?> vitals(@AuthenticationPrincipal UserPrincipal u) {
        Long pid = patientService.getPatientIdByUserId(u.getId());
        return ResponseEntity.ok(patientService.getVitals(pid));
    }

    // ── Billing ──────────────────────────────────────────
    @GetMapping("/bills")
    public ResponseEntity<?> bills(
            @RequestParam(defaultValue="0") int page,
            @AuthenticationPrincipal UserPrincipal u) {
        Long pid = patientService.getPatientIdByUserId(u.getId());
        return ResponseEntity.ok(billingService.getPatientInvoices(pid, page));
    }
}
