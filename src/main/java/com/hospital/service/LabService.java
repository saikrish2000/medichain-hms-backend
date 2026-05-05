package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabTestRepository   testRepo;
    private final LabOrderRepository  orderRepo;
    private final LabResultRepository resultRepo;
    private final PatientRepository   patientRepo;
    private final DoctorRepository    doctorRepo;
    private final UserRepository      userRepo;

    public Map<String,Object> getDashboard() {
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("totalOrders",      orderRepo.count());
        stats.put("pendingOrders",    orderRepo.countByStatus("ORDERED"));
        stats.put("processingOrders", orderRepo.countByStatus("SAMPLE_COLLECTED"));
        stats.put("completedToday",   0L);
        return stats;
    }

    public List<LabTest> getAllTests() { return testRepo.findAll(Sort.by("name")); }

    public LabTest saveTest(LabTest test) { return testRepo.save(test); }

    public Page<?> getOrders(int page, String status) {
        Pageable p = PageRequest.of(page,20,Sort.by("createdAt").descending());
        if (status != null && !status.isBlank())
            return orderRepo.findByStatus(status, p);
        return orderRepo.findAll(p);
    }

    public LabOrder getOrder(Long id) {
        return orderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LabOrder","id",id));
    }

    @Transactional
    public LabOrder markCollected(Long orderId) {
        LabOrder order = getOrder(orderId);
        order.setStatus("SAMPLE_COLLECTED");
        order.setSampleCollectedAt(LocalDateTime.now());
        return orderRepo.save(order);
    }

    @Transactional
    public Map<String,Object> uploadResults(Long orderId, Map<String,Object> body) {
        LabOrder order = getOrder(orderId);
        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        orderRepo.save(order);
        // In a full impl, save LabResult entity here
        return Map.of("message","Results uploaded","orderId", orderId,"data", body);
    }

    @Transactional
    public LabOrder createOrder(Long patientId, Long doctorId,
                                List<Long> testIds, String clinicalNotes) {
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient","id",patientId));
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor","id",doctorId));
        LabOrder order = new LabOrder();
        order.setPatient(patient);
        order.setDoctor(doctor);
        order.setClinicalNotes(clinicalNotes);
        order.setStatus("ORDERED");
        order.setCreatedAt(LocalDateTime.now());
        if (testIds != null) order.setTests(testRepo.findAllById(testIds));
        return orderRepo.save(order);
    }
}
