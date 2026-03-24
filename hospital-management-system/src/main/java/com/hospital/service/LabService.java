package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.LabOrder.OrderStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabOrderRepository labOrderRepo;
    private final LabTestRepository  labTestRepo;
    private final DoctorRepository   doctorRepo;
    private final PatientRepository  patientRepo;

    // ── TESTS ──────────────────────────────────────────────

    public Page<LabTest> getAllTests(int page) {
        return labTestRepo.findByIsActiveTrue(PageRequest.of(page, 30, Sort.by("name")));
    }

    public List<LabTest> searchTests(String query) {
        return labTestRepo.searchByName(query);
    }

    @Transactional
    public LabTest saveTest(LabTest test) {
        return labTestRepo.save(test);
    }

    // ── ORDERS ─────────────────────────────────────────────

    @Transactional
    public LabOrder createOrder(Long patientId, Long doctorId,
                                 Long appointmentId, List<Long> testIds,
                                 String clinicalNotes) {
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        String orderNumber = "LAB-" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) +
            "-" + String.format("%03d", (long)(Math.random() * 1000));

        LabOrder order = LabOrder.builder()
            .orderNumber(orderNumber)
            .patient(patient)
            .doctor(doctor)
            .status(OrderStatus.ORDERED)
            .orderedAt(LocalDateTime.now())
            .clinicalNotes(clinicalNotes)
            .build();

        List<LabResult> results = new ArrayList<>();
        for (Long testId : testIds) {
            LabTest test = labTestRepo.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("LabTest", "id", testId));
            LabResult result = LabResult.builder()
                .labOrder(order)
                .labTest(test)
                .normalRange(test.getNormalRange())
                .unit(test.getUnit())
                .flag(LabResult.ResultFlag.NORMAL)
                .build();
            results.add(result);
        }
        order.setResults(results);
        return labOrderRepo.save(order);
    }

    @Transactional
    public LabOrder collectSample(Long orderId, UserPrincipal technician) {
        LabOrder order = labOrderRepo.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("LabOrder", "id", orderId));
        order.setStatus(OrderStatus.SAMPLE_COLLECTED);
        order.setSampleCollectedAt(LocalDateTime.now());
        order.setProcessedBy(technician.getId());
        return labOrderRepo.save(order);
    }

    @Transactional
    public LabOrder enterResults(Long orderId, List<Map<String, Object>> resultData,
                                  UserPrincipal technician) {
        LabOrder order = labOrderRepo.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("LabOrder", "id", orderId));

        for (LabResult result : order.getResults()) {
            resultData.stream()
                .filter(r -> r.get("testId").toString().equals(result.getLabTest().getId().toString()))
                .findFirst()
                .ifPresent(r -> {
                    result.setResultValue((String) r.get("value"));
                    result.setRemarks((String) r.getOrDefault("remarks", ""));
                    result.setEnteredBy(technician.getId());
                    String flagStr = (String) r.getOrDefault("flag", "NORMAL");
                    result.setFlag(LabResult.ResultFlag.valueOf(flagStr));
                });
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setResultsAt(LocalDateTime.now());
        return labOrderRepo.save(order);
    }

    public Page<LabOrder> getPendingOrders(int page) {
        return labOrderRepo.findByStatusOrderByCreatedAtDesc(
            OrderStatus.ORDERED, PageRequest.of(page, 20));
    }

    public Page<LabOrder> getPatientOrders(Long patientId, int page) {
        return labOrderRepo.findByPatientIdOrderByCreatedAtDesc(
            patientId, PageRequest.of(page, 10));
    }

    public LabOrder getOrderById(Long id) {
        return labOrderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LabOrder", "id", id));
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("pendingOrders",   labOrderRepo.countByStatus(OrderStatus.ORDERED));
        stats.put("processingOrders",labOrderRepo.countByStatus(OrderStatus.PROCESSING));
        stats.put("completedToday",  labOrderRepo.countByStatus(OrderStatus.COMPLETED));
        stats.put("totalTests",      labTestRepo.count());
        stats.put("recentOrders",    labOrderRepo.findAll(
            PageRequest.of(0, 10, Sort.by("createdAt").descending())).getContent());
        return stats;
    }
}
