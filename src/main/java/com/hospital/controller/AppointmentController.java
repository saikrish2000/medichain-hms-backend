package com.hospital.controller;

import com.hospital.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService  appointmentService;
    private final AdminService        adminService;
    private final DoctorService       doctorService;

    @GetMapping("/specializations")
    public ResponseEntity<?> specializations() {
        return ResponseEntity.ok(adminService.getAllSpecializations());
    }

    @GetMapping("/branches")
    public ResponseEntity<?> branches() {
        return ResponseEntity.ok(adminService.getAllBranches());
    }

    @GetMapping("/doctors")
    public ResponseEntity<?> doctors(
            @RequestParam(required=false) Long specializationId,
            @RequestParam(required=false) Long branchId) {
        return ResponseEntity.ok(doctorService.getAvailableDoctors(specializationId, branchId));
    }

    @GetMapping("/slots")
    public ResponseEntity<?> slots(
            @RequestParam Long doctorId,
            @RequestParam String date) {
        return ResponseEntity.ok(doctorService.getAvailableSlots(doctorId, LocalDate.parse(date)));
    }

    @PostMapping("/book")
    public ResponseEntity<?> book(@RequestBody Map<String,Object> body,
                                   @AuthenticationPrincipal UserPrincipal u) {
        Long slotId  = Long.parseLong(body.get("slotId").toString());
        String reason = (String) body.getOrDefault("reasonForVisit","");
        String notes  = (String) body.getOrDefault("notes","");
        return ResponseEntity.ok(appointmentService.bookAppointment(u.getId(), slotId, reason, notes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }
}
