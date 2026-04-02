package com.hospital.controller;

import com.hospital.entity.Invoice;
import com.hospital.service.BillingService;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(billingService.getDashboardStats());
    }

    @GetMapping("/invoices")
    public ResponseEntity<?> invoices(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(billingService.getAllInvoices(page));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> invoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoiceById(id));
    }

    @PostMapping("/invoices")
    public ResponseEntity<?> create(@RequestBody Map<String,Object> body) {
        Long patId  = Long.parseLong(body.get("patientId").toString());
        Long apptId = body.containsKey("appointmentId") ?
                      Long.parseLong(body.get("appointmentId").toString()) : null;
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> items = (List<Map<String,Object>>) body.get("items");
        return ResponseEntity.ok(billingService.createInvoice(patId, apptId, items));
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<?> markPaid(@PathVariable Long id,
                                       @RequestBody Map<String,String> body) {
        Invoice.PaymentMethod method = Invoice.PaymentMethod.valueOf(
            body.getOrDefault("paymentMethod","CASH").toUpperCase());
        return ResponseEntity.ok(billingService.markAsPaid(id, method, body.get("transactionId")));
    }

    @GetMapping("/my-bills")
    public ResponseEntity<?> myBills(
            @RequestParam(defaultValue="0") int page,
            @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(billingService.getMyBills(u.getId(), page));
    }
}
