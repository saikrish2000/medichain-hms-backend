package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.security.UserPrincipal;
import com.hospital.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService   pharmacyService;
    private final PatientRepository patientRepo;
    private final DoctorRepository  doctorRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(pharmacyService.getDashboardStats());
        return "pharmacy/dashboard";
    }

    // ── MEDICINES ──────────────────────────────────────────

    @GetMapping("/medicines")
    public String medicines(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "") String search,
                            Model model) {
        if (!search.isBlank()) {
            model.addAttribute("medicines", pharmacyService.searchMedicines(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("medicines", pharmacyService.getAllMedicines(page));
        }
        model.addAttribute("categories", Medicine.Category.values());
        return "pharmacy/medicines";
    }

    @GetMapping("/medicines/new")
    public String newMedicineForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        model.addAttribute("categories", Medicine.Category.values());
        return "pharmacy/medicine-form";
    }

    @PostMapping("/medicines/save")
    public String saveMedicine(@ModelAttribute Medicine medicine, RedirectAttributes ra) {
        try {
            pharmacyService.saveMedicine(medicine);
            ra.addFlashAttribute("success", "Medicine saved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pharmacy/medicines";
    }

    @PostMapping("/medicines/{id}/stock")
    public String updateStock(@PathVariable Long id,
                              @RequestParam int quantity,
                              @RequestParam String operation,
                              RedirectAttributes ra) {
        try {
            pharmacyService.updateStock(id, quantity, operation);
            ra.addFlashAttribute("success", "Stock updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pharmacy/medicines";
    }

    // ── PRESCRIPTIONS ──────────────────────────────────────

    @GetMapping("/prescriptions")
    public String prescriptions(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("prescriptions", pharmacyService.getPendingPrescriptions(page));
        return "pharmacy/prescriptions";
    }

    @PostMapping("/prescriptions/{id}/dispense")
    public String dispense(@PathVariable Long id,
                           @AuthenticationPrincipal UserPrincipal pharmacist,
                           RedirectAttributes ra) {
        try {
            pharmacyService.dispensePrescription(id, pharmacist);
            ra.addFlashAttribute("success", "Prescription dispensed successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pharmacy/prescriptions";
    }

    // ── LOW STOCK ALERT ────────────────────────────────────
    @GetMapping("/low-stock")
    public String lowStock(Model model) {
        model.addAttribute("medicines", pharmacyService.getLowStockMedicines());
        return "pharmacy/low-stock";
    }
}
