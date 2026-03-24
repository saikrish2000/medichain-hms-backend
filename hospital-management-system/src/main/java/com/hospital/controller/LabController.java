package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.security.UserPrincipal;
import com.hospital.service.LabService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/lab")
@RequiredArgsConstructor
public class LabController {

    private final LabService        labService;
    private final PatientRepository patientRepo;
    private final DoctorRepository  doctorRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(labService.getDashboardStats());
        return "lab/dashboard";
    }

    // ── TESTS ──────────────────────────────────────────────

    @GetMapping("/tests")
    public String tests(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "") String search,
                        Model model) {
        if (!search.isBlank()) {
            model.addAttribute("tests", labService.searchTests(search));
        } else {
            model.addAttribute("tests", labService.getAllTests(page));
        }
        model.addAttribute("categories", LabTest.TestCategory.values());
        return "lab/tests";
    }

    @GetMapping("/tests/new")
    public String newTestForm(Model model) {
        model.addAttribute("test", new LabTest());
        model.addAttribute("categories", LabTest.TestCategory.values());
        return "lab/test-form";
    }

    @PostMapping("/tests/save")
    public String saveTest(@ModelAttribute LabTest test, RedirectAttributes ra) {
        try {
            labService.saveTest(test);
            ra.addFlashAttribute("success", "Test saved!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lab/tests";
    }

    // ── ORDERS ─────────────────────────────────────────────

    @GetMapping("/orders")
    public String orders(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("orders", labService.getPendingOrders(page));
        return "lab/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("order", labService.getOrderById(id));
        model.addAttribute("resultFlags", LabResult.ResultFlag.values());
        return "lab/order-detail";
    }

    @PostMapping("/orders/{id}/collect")
    public String collectSample(@PathVariable Long id,
                                @AuthenticationPrincipal UserPrincipal tech,
                                RedirectAttributes ra) {
        try {
            labService.collectSample(id, tech);
            ra.addFlashAttribute("success", "Sample collected!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lab/orders/" + id;
    }

    @PostMapping("/orders/{id}/results")
    public String enterResults(@PathVariable Long id,
                               @RequestParam List<String> testIds,
                               @RequestParam List<String> values,
                               @RequestParam List<String> flags,
                               @AuthenticationPrincipal UserPrincipal tech,
                               RedirectAttributes ra) {
        try {
            List<Map<String, Object>> resultData = new java.util.ArrayList<>();
            for (int i = 0; i < testIds.size(); i++) {
                resultData.add(Map.of(
                    "testId", testIds.get(i),
                    "value",  values.get(i),
                    "flag",   flags.get(i)
                ));
            }
            labService.enterResults(id, resultData, tech);
            ra.addFlashAttribute("success", "Results saved!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lab/orders/" + id;
    }

    // ── PHLEBOTOMIST ───────────────────────────────────────
    @GetMapping("/phlebotomist")
    public String phlebotomistDashboard(@AuthenticationPrincipal UserPrincipal user,
                                         Model model) {
        model.addAllAttributes(labService.getDashboardStats());
        return "lab/phlebotomist";
    }
}
