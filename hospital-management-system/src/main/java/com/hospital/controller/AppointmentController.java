package com.hospital.controller;

import com.hospital.entity.*;
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
import java.util.List;

@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService   appointmentService;
    private final DoctorService        doctorService;
    private final SlotService          slotService;
    private final DepartmentRepository deptRepo;
    private final SpecializationRepository specRepo;
    private final DoctorRepository     doctorRepo;
    private final PatientRepository    patientRepo;

    // ── BOOK APPOINTMENT FLOW ──────────────────────────────
    // Step 1: Choose department/specialization
    @GetMapping("/book")
    public String bookStep1(Model model) {
        model.addAttribute("departments",     deptRepo.findByIsActive(true));
        model.addAttribute("specializations", specRepo.findByIsActive(true));
        return "appointments/book-step1";
    }

    // Step 2: Choose doctor (filtered)
    @GetMapping("/book/doctors")
    public String bookStep2(@RequestParam(required = false) Long departmentId,
                             @RequestParam(required = false) Long specializationId,
                             @RequestParam(required = false) String q,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        model.addAttribute("doctors",
            doctorService.searchDoctors(q, departmentId, specializationId, page));
        model.addAttribute("departments",     deptRepo.findByIsActive(true));
        model.addAttribute("specializations", specRepo.findByIsActive(true));
        model.addAttribute("departmentId",    departmentId);
        model.addAttribute("specializationId", specializationId);
        model.addAttribute("q", q);
        return "appointments/book-step2";
    }

    // Step 3: Choose date & time slot
    @GetMapping("/book/slots")
    public String bookStep3(@RequestParam Long doctorId,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Model model) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        LocalDate selectedDate = date != null ? date : LocalDate.now().plusDays(1);
        List<DoctorSlot> slots = slotService.getAvailableSlots(doctorId, selectedDate);

        model.addAttribute("doctor",       doctor);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("slots",        slots);
        model.addAttribute("nextDate",     selectedDate.plusDays(1));
        model.addAttribute("prevDate",     selectedDate.minusDays(1));
        model.addAttribute("today",        LocalDate.now());
        return "appointments/book-step3";
    }

    // Step 4: Confirm & submit
    @GetMapping("/book/confirm")
    public String bookStep4(@RequestParam Long doctorId,
                             @RequestParam Long slotId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             @RequestParam String time,
                             Model model) {
        Doctor doctor    = doctorService.getDoctorById(doctorId);
        DoctorSlot slot  = slotService.getDoctorSlots(doctorId)
            .stream().filter(s -> s.getId().equals(slotId)).findFirst()
            .orElseThrow();
        model.addAttribute("doctor", doctor);
        model.addAttribute("slot",   slot);
        model.addAttribute("date",   date);
        model.addAttribute("time",   time);
        return "appointments/book-confirm";
    }

    @PostMapping("/book/submit")
    public String submitBooking(@AuthenticationPrincipal UserPrincipal user,
                                @RequestParam Long doctorId,
                                @RequestParam Long slotId,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam String time,
                                @RequestParam(defaultValue = "") String symptoms,
                                @RequestParam(defaultValue = "IN_PERSON") String type,
                                RedirectAttributes ra) {
        try {
            Appointment appt = appointmentService.bookAppointment(
                user.getId(), doctorId, date,
                LocalTime.parse(time), slotId, symptoms,
                Appointment.AppointmentType.valueOf(type));
            ra.addFlashAttribute("success",
                "Appointment booked! Your reference: " + appt.getAppointmentNumber());
            return "redirect:/appointments/my";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/appointments/book/slots?doctorId=" + doctorId;
        }
    }

    // ── PATIENT: MY APPOINTMENTS ───────────────────────────
    @GetMapping("/my")
    public String myAppointments(@AuthenticationPrincipal UserPrincipal user,
                                  @RequestParam(defaultValue = "0") int page,
                                  Model model) {
        Patient patient = patientRepo.findByUserId(user.getId())
            .orElse(null);
        if (patient == null) return "redirect:/patient/complete-profile";

        model.addAttribute("appointments",
            appointmentService.getPatientAppointments(patient.getId(), page));
        return "appointments/my-appointments";
    }

    @GetMapping("/my/{id}")
    public String myAppointmentDetail(@PathVariable Long id, Model model) {
        model.addAttribute("appt", appointmentService.getAppointment(id));
        return "appointments/my-appointment-detail";
    }

    @PostMapping("/my/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                    @AuthenticationPrincipal UserPrincipal user,
                                    RedirectAttributes ra) {
        try {
            appointmentService.cancelAppointment(id, user.getId());
            ra.addFlashAttribute("success", "Appointment cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointments/my";
    }
}
