package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService     appointmentService;
    private final SlotService            slotService;
    private final DepartmentRepository   deptRepo;
    private final DoctorRepository       doctorRepo;
    private final HospitalBranchRepository branchRepo;
    private final DoctorSlotRepository   slotRepo;
    private final PatientRepository      patientRepo;
    private final SpecializationRepository specRepo;

    // ── STEP 1: Choose Branch + Specialty ─────────────────
    @GetMapping("/book")
    public String step1(Model model) {
        model.addAttribute("branches", branchRepo.findAll());
        model.addAttribute("specs",    specRepo.findAll());
        return "appointments/book-step1";
    }

    // ── STEP 2: Choose Doctor ──────────────────────────────
    @GetMapping("/book/step2")
    public String step2(@RequestParam Long branchId,
                        @RequestParam Long specId,
                        Model model) {
        model.addAttribute("doctors",
            doctorRepo.findBySpecializationIdAndApprovalStatusAndBranchId(
                specId, Doctor.ApprovalStatus.APPROVED, branchId));
        model.addAttribute("branchId", branchId);
        model.addAttribute("specId",   specId);
        return "appointments/book-step2";
    }

    // ── STEP 3: Choose Slot ────────────────────────────────
    @GetMapping("/book/step3")
    public String step3(@RequestParam Long doctorId,
                        @RequestParam(required = false) String date,
                        Model model) {
        LocalDate selectedDate = (date != null && !date.isBlank())
            ? LocalDate.parse(date) : LocalDate.now().plusDays(1);
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new BadRequestException("Doctor not found"));
        List<DoctorSlot> slots = slotService.getAvailableSlots(doctorId, selectedDate);
        model.addAttribute("doctor",       doctor);
        model.addAttribute("slots",        slots);
        model.addAttribute("selectedDate", selectedDate);
        return "appointments/book-step3";
    }

    // ── STEP 4: Confirm ────────────────────────────────────
    @GetMapping("/book/confirm")
    public String confirm(@RequestParam Long slotId, Model model) {
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new BadRequestException("Slot not found"));
        model.addAttribute("slot", slot);
        return "appointments/book-confirm";
    }

    @PostMapping("/book/confirm")
    public String doBook(@RequestParam Long slotId,
                         @RequestParam(required = false) String reason,
                         @RequestParam(required = false) String notes,
                         @AuthenticationPrincipal UserPrincipal user,
                         RedirectAttributes ra) {
        try {
            Appointment appt = appointmentService.bookAppointment(
                user.getId(), slotId,
                reason != null ? reason : "General consultation", notes);
            ra.addFlashAttribute("success",
                "Appointment booked! Reference: " + appt.getAppointmentNumber());
            return "redirect:/appointments/my";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/appointments/book/confirm?slotId=" + slotId;
        }
    }

    // ── MY APPOINTMENTS ────────────────────────────────────
    @GetMapping("/my")
    public String myAppointments(@AuthenticationPrincipal UserPrincipal user,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model) {
        Patient patient = patientRepo.findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException("Patient profile not found"));
        model.addAttribute("appointments",
            appointmentService.getPatientAppointments(patient.getId(), page));
        return "appointments/my-appointments";
    }

    // ── CANCEL ─────────────────────────────────────────────
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                          @AuthenticationPrincipal UserPrincipal user,
                          RedirectAttributes ra) {
        try {
            appointmentService.cancelByPatient(id, user.getId());
            ra.addFlashAttribute("success", "Appointment cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointments/my";
    }
}
