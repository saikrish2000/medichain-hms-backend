package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.security.UserPrincipal;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('PATIENT')")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService       patientService;
    private final AppointmentService   appointmentService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard(@AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(patientService.getDashboard(me.getId()));
    }

    @GetMapping("/profile")
    public ResponseEntity<Patient> profile(@AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(patientService.getProfile(me.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<Patient> updateProfile(@AuthenticationPrincipal UserPrincipal me,
                                                 @RequestBody Patient data) {
        return ResponseEntity.ok(patientService.updateProfile(me.getId(), data));
    }

    @GetMapping("/appointments")
    public ResponseEntity<Page<Appointment>> myAppointments(@AuthenticationPrincipal UserPrincipal me,
                                                            @RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(me.getId(), page));
    }

    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserPrincipal me) {
        appointmentService.cancelByPatient(id, me.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/records")
    public ResponseEntity<List<MedicalRecord>> myRecords(@AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(medicalRecordService.getPatientRecordsList(me.getId()));
    }

    @GetMapping("/vitals")
    public ResponseEntity<List<MedicalRecord>> myVitals(@AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(medicalRecordService.getPatientVitals(me.getId()));
    }
}
