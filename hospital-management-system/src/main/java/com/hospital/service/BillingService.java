package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.Invoice.InvoiceStatus;
import com.hospital.entity.Invoice.PaymentMethod;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InvoiceRepository  invoiceRepo;
    private final PatientRepository  patientRepo;
    private final AppointmentRepository appointmentRepo;
    private final HospitalBranchRepository branchRepo;

    // ── INVOICE GENERATION ─────────────────────────────────

    @Transactional
    public Invoice createInvoice(Long patientId, Long appointmentId,
                                 List<Map<String, Object>> items, String notes) {
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        String invoiceNo = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
            .invoiceNumber(invoiceNo)
            .patient(patient)
            .invoiceDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(30))
            .status(InvoiceStatus.PENDING)
            .notes(notes)
            .build();

        if (appointmentId != null) {
            appointmentRepo.findById(appointmentId).ifPresent(invoice::setAppointment);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<InvoiceItem> invoiceItems = new ArrayList<>();

        for (Map<String, Object> itemMap : items) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setDescription((String) itemMap.get("description"));
            item.setItemType(InvoiceItem.ItemType.valueOf((String) itemMap.getOrDefault("type", "CONSULTATION")));
            item.setQuantity((Integer) itemMap.getOrDefault("quantity", 1));
            item.setUnitPrice(new BigDecimal(itemMap.get("unitPrice").toString()));
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            invoiceItems.add(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.05")); // 5% GST
        invoice.setItems(invoiceItems);
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(tax);
        invoice.setTotalAmount(subtotal.add(tax));
        invoice.setPaidAmount(BigDecimal.ZERO);

        return invoiceRepo.save(invoice);
    }

    @Transactional
    public Invoice createConsultationInvoice(Long patientId, Long appointmentId,
                                              BigDecimal consultationFee) {
        List<Map<String, Object>> items = List.of(
            Map.of("description", "Consultation Fee", "type", "CONSULTATION",
                   "quantity", 1, "unitPrice", consultationFee)
        );
        return createInvoice(patientId, appointmentId, items, "Consultation charge");
    }

    // ── PAYMENT PROCESSING ─────────────────────────────────

    @Transactional
    public Invoice markAsPaid(Long invoiceId, PaymentMethod method, String transactionId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAmount(invoice.getTotalAmount());
        invoice.setPaymentMethod(method);
        invoice.setPaymentDate(LocalDateTime.now());
        if (transactionId != null) invoice.setRazorpayPaymentId(transactionId);

        return invoiceRepo.save(invoice);
    }

    @Transactional
    public Invoice processPartialPayment(Long invoiceId, BigDecimal amount, PaymentMethod method) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        BigDecimal newPaid = invoice.getPaidAmount().add(amount);
        invoice.setPaidAmount(newPaid);
        invoice.setPaymentMethod(method);
        invoice.setPaymentDate(LocalDateTime.now());

        if (newPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIAL);
        }

        return invoiceRepo.save(invoice);
    }

    // ── QUERIES ────────────────────────────────────────────

    public Page<Invoice> getAllInvoices(int page, int size) {
        return invoiceRepo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Page<Invoice> getPatientInvoices(Long patientId, int page) {
        return invoiceRepo.findByPatientId(patientId, PageRequest.of(page, 10, Sort.by("createdAt").descending()));
    }

    public Page<Invoice> getPendingInvoices(int page) {
        return invoiceRepo.findByStatus(InvoiceStatus.PENDING, PageRequest.of(page, 20));
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        stats.put("todayRevenue",    invoiceRepo.sumPaidByDate(today));
        stats.put("monthRevenue",    invoiceRepo.sumPaidBetween(monthStart, today));
        stats.put("pendingCount",    invoiceRepo.countByStatus(InvoiceStatus.PENDING));
        stats.put("todayInvoices",   invoiceRepo.countByInvoiceDate(today));
        stats.put("recentInvoices",  invoiceRepo.findAll(
            PageRequest.of(0, 10, Sort.by("createdAt").descending())).getContent());
        return stats;
    }

    // ── HELPERS ────────────────────────────────────────────

    private String generateInvoiceNumber() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        long count = invoiceRepo.count() + 1;
        return prefix + String.format("%04d", count);
    }
}
