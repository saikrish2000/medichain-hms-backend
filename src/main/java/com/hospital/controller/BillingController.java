package com.hospital.controller;

import com.hospital.service.BillingService;
import com.hospital.service.InvoicePdfService;
import com.hospital.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Invoice and payment management")
public class BillingController {

    private final BillingService    billingService;
    private final InvoicePdfService invoicePdfService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(billingService.getDashboardStats());
    }

    @GetMapping("/invoices")
    public ResponseEntity<?> invoices(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(billingService.getAllInvoices(page));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> invoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoiceById(id));
    }

    @PostMapping("/invoices")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long patId  = Long.parseLong(body.get("patientId").toString());
        Long apptId = body.containsKey("appointmentId")
                    ? Long.parseLong(body.get("appointmentId").toString()) : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        return ResponseEntity.ok(billingService.createInvoice(patId, apptId, items));
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<?> markPaid(@PathVariable Long id,
                                       @RequestBody Map<String, String> body) {
        String method = body.getOrDefault("paymentMethod", "CASH").toUpperCase();
        return ResponseEntity.ok(billingService.markAsPaid(id, method, body.get("transactionId")));
    }

    /** Download invoice as PDF */
    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = invoicePdfService.generateInvoicePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/my-bills")
    public ResponseEntity<?> myBills(@RequestParam(defaultValue = "0") int page,
                                      @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(billingService.getMyBills(u.getId(), page));
    }
}
