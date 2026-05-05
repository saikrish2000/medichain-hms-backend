package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final InvoiceRepository      invoiceRepo;
    private final PatientRepository      patientRepo;
    private final AppointmentRepository  appointmentRepo;
    private final InvoicePdfService      pdfService;

    // ── Controller-facing methods ─────────────────────────────────────────

    public Map<String,Object> getDashboard() {
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("totalRevenue",   invoiceRepo.sumPaidAmount().orElse(BigDecimal.ZERO));
        stats.put("totalInvoices",  invoiceRepo.count());
        stats.put("pendingInvoices",invoiceRepo.countByStatus("PENDING"));
        stats.put("paidInvoices",   invoiceRepo.countByStatus("PAID"));
        stats.put("todayCollection",BigDecimal.ZERO); // extend with date query if needed
        return stats;
    }

    public Page<Invoice> getInvoices(int page, String status) {
        if (status != null && !status.isBlank())
            return invoiceRepo.findByStatus(status, PageRequest.of(page,20,Sort.by("createdAt").descending()));
        return invoiceRepo.findAll(PageRequest.of(page,20,Sort.by("createdAt").descending()));
    }

    public Invoice getInvoice(Long id) {
        return invoiceRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice","id",id));
    }

    @Transactional
    public Invoice createInvoice(Map<String,Object> body) {
        Long patientId     = Long.parseLong(body.get("patientId").toString());
        Long appointmentId = body.get("appointmentId") != null
            ? Long.parseLong(body.get("appointmentId").toString()) : null;

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> itemsData = (List<Map<String,Object>>) body.getOrDefault("items", List.of());

        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient","id",patientId));

        Invoice invoice = new Invoice();
        invoice.setPatient(patient);
        invoice.setStatus("PENDING");
        invoice.setCreatedAt(LocalDateTime.now());
        String invNum = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        invoice.setInvoiceNumber(invNum);

        if (appointmentId != null)
            appointmentRepo.findById(appointmentId).ifPresent(invoice::setAppointment);

        List<InvoiceItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String,Object> d : itemsData) {
            InvoiceItem item = new InvoiceItem();
            item.setDescription((String) d.get("description"));
            BigDecimal qty   = new BigDecimal(d.get("quantity").toString());
            BigDecimal price = new BigDecimal(d.get("unitPrice").toString());
            item.setQuantity(qty.intValue());
            item.setUnitPrice(price);
            item.setTotalPrice(price.multiply(qty));
            item.setInvoice(invoice);
            items.add(item);
            total = total.add(item.getTotalPrice());
        }
        invoice.setItems(items);
        invoice.setTotalAmount(total);
        return invoiceRepo.save(invoice);
    }

    @Transactional
    public Invoice recordPayment(Long invoiceId, Map<String,Object> body) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice","id",invoiceId));
        if ("PAID".equals(invoice.getStatus()))
            throw new BadRequestException("Invoice already paid");
        invoice.setStatus("PAID");
        invoice.setPaymentMethod((String) body.getOrDefault("method","CASH"));
        if (body.get("transactionId") != null)
            invoice.setTransactionId(body.get("transactionId").toString());
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setAmountPaid(invoice.getTotalAmount());
        return invoiceRepo.save(invoice);
    }

    public ResponseEntity<?> downloadPdf(Long invoiceId) {
        try {
            Invoice invoice = getInvoice(invoiceId);
            byte[] pdf = pdfService.generate(invoice);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + invoiceId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error","PDF generation failed: " + e.getMessage()));
        }
    }

    public Page<Invoice> getPatientBills(Long userId, int page) {
        Patient patient = patientRepo.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Patient profile not found"));
        return invoiceRepo.findByPatientId(patient.getId(),
            PageRequest.of(page,15,Sort.by("createdAt").descending()));
    }
}
