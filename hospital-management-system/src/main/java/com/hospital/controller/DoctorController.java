package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.service.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    private final DoctorService       doctorService;
    private final AppointmentService  appointmentService;
    private final SlotService         slotService;
    private final PharmacyService     pharmacyService;
    private final LabService          labService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard(
            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(doctorService.getDashboard(u.getId()));
    }

    // ── Appointments ─────────────────────────────────────
    @GetMapping("/appointments")
    public ResponseEntity<?> appointments(
            @RequestParam(defaultValue="0") int page,
            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(appointmentService.getDoctorAllAppointments(
            doctorService.getDoctorIdByUserId(u.getId()), page));
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<?> todayAppointments(@AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(appointmentService.getTodayAppointments(
            doctorService.getDoctorIdByUserId(u.getId())));
    }

    @PostMapping("/appointments/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id,
                                      @AuthenticationPrincipal UserPrincipal u) {
        appointmentService.confirmAppointment(id, u.getId());
        return ResponseEntity.ok(Map.of("message","Appointment confirmed"));
    }

    @PostMapping("/appointments/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                     @RequestBody Map<String,String> body,
                                     @AuthenticationPrincipal UserPrincipal u) {
        appointmentService.rejectAppointment(id, u.getId(), body.get("reason"));
        return ResponseEntity.ok(Map.of("message","Appointment rejected"));
    }

    @PostMapping("/appointments/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal u) {
        appointmentService.completeAppointment(id, u.getId());
        return ResponseEntity.ok(Map.of("message","Appointment completed"));
    }

    @PostMapping("/appointments/{id}/no-show")
    public ResponseEntity<?> noShow(@PathVariable Long id,
                                     @AuthenticationPrincipal UserPrincipal u) {
        appointmentService.markNoShow(id, u.getId());
        return ResponseEntity.ok(Map.of("message","Marked as no-show"));
    }

    // ── Slots ────────────────────────────────────────────
    @GetMapping("/slots")
    public ResponseEntity<?> slots(@AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(slotService.getDoctorSlots(
            doctorService.getDoctorIdByUserId(u.getId())));
    }

    @PostMapping("/slots/specific")
    public ResponseEntity<?> createSpecific(@RequestBody Map<String,Object> body,
                                             @AuthenticationPrincipal UserPrincipal u) {
        Long docId = doctorService.getDoctorIdByUserId(u.getId());
        DoctorSlot form = new DoctorSlot();
        form.setSlotDate(LocalDate.parse((String) body.get("slotDate")));
        form.setStartTime(LocalTime.parse((String) body.get("startTime")));
        form.setEndTime(LocalTime.parse((String) body.get("endTime")));
        form.setDurationMinutes((Integer) body.getOrDefault("durationMinutes", 30));
        form.setMaxPatients((Integer) body.getOrDefault("maxPatients", 1));
        return ResponseEntity.ok(slotService.createSpecificSlot(docId, form));
    }

    @PostMapping("/slots/{id}/block")
    public ResponseEntity<?> blockSlot(@PathVariable Long id,
                                        @RequestBody Map<String,String> body) {
        slotService.blockSlot(id, body.getOrDefault("reason","Blocked"));
        return ResponseEntity.ok(Map.of("message","Slot blocked"));
    }

    @PostMapping("/slots/{id}/unblock")
    public ResponseEntity<?> unblockSlot(@PathVariable Long id) {
        slotService.unblockSlot(id);
        return ResponseEntity.ok(Map.of("message","Slot unblocked"));
    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
        return ResponseEntity.ok(Map.of("message","Slot deleted"));
    }

    // ── Patients ─────────────────────────────────────────
    @GetMapping("/patients")
    public ResponseEntity<?> patients(
            @RequestParam(defaultValue="0") int page,
            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(doctorService.getDoctorPatients(u.getId(), page));
    }

    // ── Medical Records ──────────────────────────────────
    @GetMapping("/records/{patientId}")
    public ResponseEntity<?> records(@PathVariable Long patientId,
                                      @RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(medicalRecordService.getPatientRecords(patientId, page));
    }

    @PostMapping("/records")
    public ResponseEntity<?> addRecord(@RequestBody MedicalRecord record,
                                        @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(medicalRecordService.addRecord(record, u.getId()));
    }

    // ── Prescriptions ────────────────────────────────────
    @PostMapping("/prescriptions")
    public ResponseEntity<?> createPrescription(@RequestBody Map<String,Object> body,
                                                  @AuthenticationPrincipal UserPrincipal u) {
        Long docId    = doctorService.getDoctorIdByUserId(u.getId());
        Long patId    = Long.parseLong(body.get("patientId").toString());
        Long apptId   = body.containsKey("appointmentId") ?
                        Long.parseLong(body.get("appointmentId").toString()) : null;
        String notes  = (String) body.getOrDefault("notes","");
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> items = (List<Map<String,Object>>) body.get("items");
        return ResponseEntity.ok(pharmacyService.createPrescription(patId, docId, apptId, notes, items));
    }

    // ── Lab Orders ───────────────────────────────────────
    @GetMapping("/lab-orders")
    public ResponseEntity<?> labOrders(
            @RequestParam(defaultValue="0") int page,
            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(labService.getDoctorOrders(
            doctorService.getDoctorIdByUserId(u.getId()), page));
    }

    @PostMapping("/lab-orders")
    public ResponseEntity<?> createLabOrder(@RequestBody Map<String,Object> body,
                                             @AuthenticationPrincipal UserPrincipal u) {
        Long docId  = doctorService.getDoctorIdByUserId(u.getId());
        Long patId  = Long.parseLong(body.get("patientId").toString());
        String notes = (String) body.getOrDefault("clinicalNotes","");
        @SuppressWarnings("unchecked")
        List<Long> testIds = (List<Long>) body.get("testIds");
        return ResponseEntity.ok(labService.createOrder(patId, docId, testIds, notes));
    }
}
