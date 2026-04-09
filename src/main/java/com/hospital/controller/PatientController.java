package com.hospital.controller;

import com.hospital.entity.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.service.*;
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

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('PATIENT')")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard(@AuthenticationPrincipal UserPrincipal me) {
        return ResponseEntity.ok(patientService.getDashboard(me.getId()));
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
