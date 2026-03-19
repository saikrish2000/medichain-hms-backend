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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService       doctorService;
    private final AppointmentService  appointmentService;
    private final SlotService         slotService;
    private final PatientRepository   patientRepo;
    private final DoctorRepository    doctorRepo;

    // ── DASHBOARD ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal user, Model model) {
        model.addAllAttributes(doctorService.getDashboardData(user.getId()));
        return "doctor/dashboard";
    }

    // ── APPOINTMENTS ───────────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserPrincipal user,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "pending") String filter,
                               Model model) {
        Doctor doctor = doctorService.getByUserId(user.getId());
        if ("pending".equals(filter)) {
            model.addAttribute("appointments",
                appointmentService.getDoctorPendingAppointments(doctor.getId(), page));
        } else {
            model.addAttribute("appointments",
                appointmentService.getDoctorAllAppointments(doctor.getId(), page));
        }
        model.addAttribute("filter", filter);
        model.addAttribute("doctor", doctor);
        return "doctor/appointments";
    }

    @PostMapping("/appointments/{id}/confirm")
    public String confirmAppointment(@PathVariable Long id,
                                      @AuthenticationPrincipal UserPrincipal user,
                                      RedirectAttributes ra) {
        try {
            appointmentService.confirmAppointment(id, user.getId());
            ra.addFlashAttribute("success", "Appointment confirmed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/appointments/{id}/reject")
    public String rejectAppointment(@PathVariable Long id,
                                     @RequestParam(defaultValue = "Schedule conflict") String reason,
                                     @AuthenticationPrincipal UserPrincipal user,
                                     RedirectAttributes ra) {
        try {
            appointmentService.rejectAppointment(id, user.getId(), reason);
            ra.addFlashAttribute("success", "Appointment rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal user,
                                       RedirectAttributes ra) {
        appointmentService.completeAppointment(id, user.getId());
        ra.addFlashAttribute("success", "Appointment marked as complete.");
        return "redirect:/doctor/appointments";
    }

    // ── SLOTS ──────────────────────────────────────────────
    @GetMapping("/slots")
    public String slots(@AuthenticationPrincipal UserPrincipal user,
                        @RequestParam(defaultValue = "0") int weekOffset,
                        Model model) {
        Doctor doctor = doctorService.getByUserId(user.getId());
        LocalDate weekStart = LocalDate.now().plusWeeks(weekOffset).with(
            java.time.DayOfWeek.MONDAY);
        model.addAttribute("doctor",     doctor);
        model.addAttribute("weekSlots",  slotService.getWeekCalendar(doctor.getId(), weekStart));
        model.addAttribute("weekStart",  weekStart);
        model.addAttribute("weekOffset", weekOffset);
        return "doctor/slots";
    }

    @PostMapping("/slots/add-specific")
    public String addSpecificSlot(@AuthenticationPrincipal UserPrincipal user,
                                   @ModelAttribute DoctorSlot form,
                                   RedirectAttributes ra) {
        try {
            Doctor doctor = doctorService.getByUserId(user.getId());
            slotService.createSpecificSlot(doctor.getId(), form);
            ra.addFlashAttribute("success", "Slot added.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/slots";
    }

    @PostMapping("/slots/add-recurring")
    public String addRecurringSlot(@AuthenticationPrincipal UserPrincipal user,
                                    @RequestParam String dayOfWeek,
                                    @RequestParam String slotTime,
                                    @RequestParam(defaultValue = "30") int durationMinutes,
                                    @RequestParam(defaultValue = "1") int maxPatients,
                                    @RequestParam(required = false) String endDate,
                                    RedirectAttributes ra) {
        try {
            Doctor doctor = doctorService.getByUserId(user.getId());
            LocalDate end = (endDate != null && !endDate.isBlank())
                ? LocalDate.parse(endDate) : LocalDate.now().plusWeeks(8);
            slotService.createRecurringSlots(doctor.getId(),
                java.time.DayOfWeek.valueOf(dayOfWeek.toUpperCase()),
                java.time.LocalTime.parse(slotTime),
                durationMinutes, maxPatients,
                LocalDate.now(), end);
            ra.addFlashAttribute("success", "Recurring slots created.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/slots";
    }

    @PostMapping("/slots/{id}/toggle-block")
    public String toggleSlotBlock(@PathVariable Long id, RedirectAttributes ra) {
        slotService.toggleSlotBlock(id);
        ra.addFlashAttribute("success", "Slot status updated.");
        return "redirect:/doctor/slots";
    }

    // ── PATIENTS ───────────────────────────────────────────
    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserPrincipal user,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        model.addAttribute("patients",
            doctorService.getDoctorPatients(user.getId(), page));
        return "doctor/patients";
    }

    // ── PLACEHOLDER ROUTES ─────────────────────────────────
    @GetMapping("/prescriptions")
    public String prescriptions(Model model) { return "doctor/prescriptions"; }

    @GetMapping("/telehealth")
    public String telehealth(Model model) { return "doctor/telehealth"; }

    @GetMapping("/ot-planner")
    public String otPlanner(Model model) { return "doctor/ot-planner"; }
}
