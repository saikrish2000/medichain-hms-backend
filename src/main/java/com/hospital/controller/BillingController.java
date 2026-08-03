package com.hospital.controller;

import com.hospital.security.UserPrincipal;
import com.hospital.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(billingService.getDashboard());
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<?> invoices(@RequestParam(defaultValue="0") int page,
                                      @RequestParam(required=false) String status) {
        return ResponseEntity.ok(billingService.getInvoices(page, status));
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public ResponseEntity<?> invoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoice(id));
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<?> createInvoice(@RequestBody Map<String,Object> body) {
        return ResponseEntity.ok(billingService.createInvoice(body));
    }

    @PostMapping("/invoices/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public ResponseEntity<?> pay(@PathVariable Long id,
                                 @RequestBody Map<String,Object> body) {
        return ResponseEntity.ok(billingService.recordPayment(id, body));
    }

    @GetMapping("/invoices/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public ResponseEntity<?> downloadPdf(@PathVariable Long id) {
        return billingService.downloadPdf(id);
    }

    @GetMapping("/my-bills")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> myBills(@AuthenticationPrincipal UserPrincipal me,
                                     @RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(billingService.getPatientBills(me.getId(), page));
    }
}
