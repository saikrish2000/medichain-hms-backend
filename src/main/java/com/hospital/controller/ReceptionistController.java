package com.hospital.controller;

import com.hospital.service.AdminService;
import com.hospital.service.AppointmentService;
import com.hospital.service.ReceptionistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/receptionist")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
public class ReceptionistController {

    private final ReceptionistService receptionistService;
    private final AppointmentService  appointmentService;
    private final AdminService        adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(receptionistService.getDashboardStats());
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<?> todayAppts() {
        return ResponseEntity.ok(receptionistService.getTodayAppointments());
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> appointments(@RequestParam(required=false) String date,
                                          @RequestParam(defaultValue="0") int page) {
        LocalDate d = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(receptionistService.getAppointmentsByDate(d, page));
    }

    @PostMapping("/appointments/{id}/checkin")
    public ResponseEntity<?> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(receptionistService.checkIn(id));
    }

    @GetMapping("/patients/search")
    public ResponseEntity<?> searchPatients(@RequestParam String q) {
        return ResponseEntity.ok(receptionistService.searchPatients(q));
    }

    @PostMapping("/appointments/book")
    public ResponseEntity<?> bookAppointment(@RequestBody java.util.Map<String,Object> body) {
        return ResponseEntity.ok(appointmentService.bookByReceptionist(body));
    }
}
