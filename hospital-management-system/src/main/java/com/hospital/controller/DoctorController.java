package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.entity.DoctorSlot.SlotType;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService        doctorService;
    private final SlotService          slotService;
    private final AppointmentService   appointmentService;
    private final MedicalRecordRepository recordRepo;
    private final DepartmentRepository deptRepo;

    // ── DASHBOARD ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        if (doctor.getApprovalStatus() == Doctor.ApprovalStatus.PENDING) {
            model.addAttribute("doctor", doctor);
            return "doctor/pending-approval";
        }
        Map<String, Object> data = doctorService.getDoctorDashboard(doctor.getId());
        model.addAllAttributes(data);
        model.addAttribute("doctor", doctor);
        model.addAttribute("today", LocalDate.now());
        return "doctor/dashboard";
    }

    // ── PROFILE ────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        model.addAttribute("doctor", doctor);
        return "doctor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserPrincipal user,
                                @ModelAttribute Doctor form,
                                RedirectAttributes ra) {
        doctorService.updateProfile(user.getId(), form);
        ra.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/doctor/profile";
    }

    // ── SLOTS ──────────────────────────────────────────────
    @GetMapping("/slots")
    public String slots(@AuthenticationPrincipal UserPrincipal user,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
                        Model model) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        LocalDate weekStart = (week != null ? week : LocalDate.now())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        model.addAttribute("doctor",   doctor);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd",   weekStart.plusDays(6));
        model.addAttribute("prevWeek",  weekStart.minusWeeks(1));
        model.addAttribute("nextWeek",  weekStart.plusWeeks(1));
        model.addAttribute("calendar",  slotService.getWeekCalendar(doctor.getId(), weekStart));
        model.addAttribute("allSlots",  slotService.getDoctorSlots(doctor.getId()));
        model.addAttribute("days",      DayOfWeek.values());
        model.addAttribute("today",     LocalDate.now());
        return "doctor/slots";
    }

    @PostMapping("/slots/create-specific")
    public String createSpecificSlot(@AuthenticationPrincipal UserPrincipal user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotDate,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "15") int duration,
            @RequestParam(defaultValue = "1") int maxPatients,
            RedirectAttributes ra) {
        try {
            Doctor doctor = doctorService.getDoctorByUserId(user.getId());
            slotService.createSpecificSlot(doctor.getId(), slotDate,
                LocalTime.parse(startTime), LocalTime.parse(endTime),
                duration, maxPatients);
            ra.addFlashAttribute("success", "Slot created for " + slotDate);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/slots";
    }

    @PostMapping("/slots/create-recurring")
    public String createRecurringSlot(@AuthenticationPrincipal UserPrincipal user,
            @RequestParam String dayOfWeek,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "15") int duration,
            @RequestParam(defaultValue = "1") int maxPatients,
            RedirectAttributes ra) {
        try {
            Doctor doctor = doctorService.getDoctorByUserId(user.getId());
            slotService.createRecurringSlot(doctor.getId(),
                DayOfWeek.valueOf(dayOfWeek),
                LocalTime.parse(startTime), LocalTime.parse(endTime),
                duration, maxPatients);
            ra.addFlashAttribute("success", "Recurring slot added for every " + dayOfWeek);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/doctor/slots";
    }

    @PostMapping("/slots/{id}/block")
    public String blockSlot(@PathVariable Long id,
                            @RequestParam(defaultValue = "") String reason,
                            RedirectAttributes ra) {
        slotService.blockSlot(id, reason);
        ra.addFlashAttribute("success", "Slot blocked.");
        return "redirect:/doctor/slots";
    }

    @PostMapping("/slots/{id}/unblock")
    public String unblockSlot(@PathVariable Long id, RedirectAttributes ra) {
        slotService.unblockSlot(id);
        ra.addFlashAttribute("success", "Slot restored.");
        return "redirect:/doctor/slots";
    }

    @PostMapping("/slots/{id}/delete")
    public String deleteSlot(@PathVariable Long id, RedirectAttributes ra) {
        slotService.deleteSlot(id);
        ra.addFlashAttribute("success", "Slot removed.");
        return "redirect:/doctor/slots";
    }

    // ── APPOINTMENTS ───────────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserPrincipal user,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "all") String filter,
                               Model model) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        if ("pending".equals(filter))
            model.addAttribute("appointments",
                appointmentService.getDoctorPendingAppointments(doctor.getId(), page));
        else
            model.addAttribute("appointments",
                appointmentService.getDoctorAppointments(doctor.getId(), page));
        model.addAttribute("filter", filter);
        model.addAttribute("doctor", doctor);
        return "doctor/appointments";
    }

    @GetMapping("/appointments/{id}")
    public String appointmentDetail(@PathVariable Long id,
                                    @AuthenticationPrincipal UserPrincipal user,
                                    Model model) {
        Appointment appt = appointmentService.getAppointment(id);
        model.addAttribute("appt", appt);
        model.addAttribute("records",
            recordRepo.findTop5ByPatientIdOrderByVisitDateDesc(appt.getPatient().getId()));
        return "doctor/appointment-detail";
    }

    @PostMapping("/appointments/{id}/confirm")
    public String confirmAppt(@PathVariable Long id,
                              @RequestParam(defaultValue = "") String notes,
                              @AuthenticationPrincipal UserPrincipal user,
                              RedirectAttributes ra) {
        appointmentService.confirmAppointment(id, notes, user);
        ra.addFlashAttribute("success", "Appointment confirmed!");
        return "redirect:/doctor/appointments?filter=pending";
    }

    @PostMapping("/appointments/{id}/reject")
    public String rejectAppt(@PathVariable Long id,
                             @RequestParam String reason,
                             @AuthenticationPrincipal UserPrincipal user,
                             RedirectAttributes ra) {
        appointmentService.rejectAppointment(id, reason, user);
        ra.addFlashAttribute("success", "Appointment rejected.");
        return "redirect:/doctor/appointments?filter=pending";
    }

    @PostMapping("/appointments/{id}/complete")
    public String completeAppt(@PathVariable Long id,
                               @AuthenticationPrincipal UserPrincipal user,
                               RedirectAttributes ra) {
        appointmentService.completeAppointment(id, user);
        ra.addFlashAttribute("success", "Appointment marked as completed.");
        return "redirect:/doctor/appointments";
    }

    // ── PATIENTS ───────────────────────────────────────────
    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserPrincipal user, Model model) {
        Doctor doctor = doctorService.getDoctorByUserId(user.getId());
        model.addAttribute("records", recordRepo.findByDoctorIdOrderByVisitDateDesc(doctor.getId()));
        model.addAttribute("doctor", doctor);
        return "doctor/patients";
    }
}
