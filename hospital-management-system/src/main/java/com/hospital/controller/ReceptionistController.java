package com.hospital.controller;

import com.hospital.service.ReceptionistService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/receptionist")
@RequiredArgsConstructor
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(receptionistService.getDashboardStats());
        return "receptionist/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("appointments", receptionistService.searchAppointments(date, page));
        model.addAttribute("selectedDate", date != null ? date : LocalDate.now());
        return "receptionist/appointments";
    }

    @PostMapping("/appointments/{id}/checkin")
    public String checkIn(@PathVariable Long id, RedirectAttributes ra) {
        try {
            receptionistService.checkIn(id);
            ra.addFlashAttribute("success", "Patient checked in!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/receptionist/appointments";
    }
}
