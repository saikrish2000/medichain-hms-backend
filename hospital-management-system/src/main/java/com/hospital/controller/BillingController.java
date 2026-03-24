package com.hospital.controller;

import com.hospital.entity.Invoice;
import com.hospital.entity.Invoice.PaymentMethod;
import com.hospital.repository.PatientRepository;
import com.hospital.security.UserPrincipal;
import com.hospital.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService    billingService;
    private final PatientRepository patientRepo;

    // ── ADMIN / RECEPTIONIST BILLING DASHBOARD ─────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = billingService.getDashboardStats();
        model.addAllAttributes(stats);
        return "billing/dashboard";
    }

    // ── LIST ALL INVOICES ──────────────────────────────────
    @GetMapping("/invoices")
    public String listInvoices(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Invoice> invoices = billingService.getAllInvoices(page, 20);
        model.addAttribute("invoices", invoices);
        return "billing/invoices";
    }

    // ── CREATE INVOICE FORM ────────────────────────────────
    @GetMapping("/invoices/new")
    public String newInvoiceForm(Model model) {
        model.addAttribute("patients", patientRepo.findAll());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "billing/invoice-form";
    }

    // ── CREATE INVOICE ─────────────────────────────────────
    @PostMapping("/invoices/create")
    public String createInvoice(
            @RequestParam Long patientId,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam String notes,
            @RequestParam List<String> descriptions,
            @RequestParam List<String> types,
            @RequestParam List<Integer> quantities,
            @RequestParam List<BigDecimal> unitPrices,
            RedirectAttributes ra) {
        try {
            List<Map<String, Object>> items = new ArrayList<>();
            for (int i = 0; i < descriptions.size(); i++) {
                items.add(Map.of(
                    "description", descriptions.get(i),
                    "type", types.get(i),
                    "quantity", quantities.get(i),
                    "unitPrice", unitPrices.get(i)
                ));
            }
            Invoice invoice = billingService.createInvoice(patientId, appointmentId, items, notes);
            ra.addFlashAttribute("success", "Invoice " + invoice.getInvoiceNumber() + " created!");
            return "redirect:/billing/invoices/" + invoice.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/billing/invoices/new";
        }
    }

    // ── VIEW INVOICE ───────────────────────────────────────
    @GetMapping("/invoices/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Invoice invoice = billingService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "billing/invoice-detail";
    }

    // ── MARK AS PAID ───────────────────────────────────────
    @PostMapping("/invoices/{id}/pay")
    public String markPaid(@PathVariable Long id,
                           @RequestParam String paymentMethod,
                           @RequestParam(required = false) String transactionId,
                           RedirectAttributes ra) {
        try {
            billingService.markAsPaid(id, PaymentMethod.valueOf(paymentMethod), transactionId);
            ra.addFlashAttribute("success", "Payment recorded successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/billing/invoices/" + id;
    }

    // ── PATIENT VIEW THEIR BILLS ───────────────────────────
    @GetMapping("/my-bills")
    public String myBills(@AuthenticationPrincipal UserPrincipal user,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        // Find patient by user
        patientRepo.findByUserId(user.getId()).ifPresent(patient -> {
            model.addAttribute("invoices", billingService.getPatientInvoices(patient.getId(), page));
            model.addAttribute("patient", patient);
        });
        return "billing/my-bills";
    }
}
