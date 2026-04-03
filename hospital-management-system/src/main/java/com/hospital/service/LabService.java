package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
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

    public List<LabTest> getAllTests() { return testRepo.findAll(Sort.by("name")); }

    public LabTest saveTest(LabTest test) { return testRepo.save(test); }

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

    @Transactional
    public LabOrder collectSample(Long orderId, UserPrincipal technician) {
        LabOrder order = orderRepo.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("LabOrder","id",orderId));
        order.setStatus("SAMPLE_COLLECTED");
        order.setSampleCollectedAt(LocalDateTime.now());
        userRepo.findById(technician.getId()).ifPresent(order::setCollectedBy);
        return orderRepo.save(order);
    }

    @Transactional
    public LabOrder enterResults(Long orderId, List<Map<String,Object>> resultData,
                                 String notes, UserPrincipal technician) {
        LabOrder order = orderRepo.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("LabOrder","id",orderId));
        List<LabResult> results = new ArrayList<>();
        for (Map<String,Object> rd : resultData) {
            LabResult r = new LabResult();
            r.setOrder(order);
            r.setTestName((String) rd.get("testName"));
            r.setResult((String) rd.get("result"));
            r.setUnit((String) rd.getOrDefault("unit",""));
            r.setReferenceRange((String) rd.getOrDefault("referenceRange",""));
            r.setIsAbnormal(Boolean.TRUE.equals(rd.get("isAbnormal")));
            results.add(r);
        }
        resultRepo.saveAll(results);
        order.setResults(results);
        order.setStatus("COMPLETED");
        order.setResultNotes(notes);
        order.setCompletedAt(LocalDateTime.now());
        return orderRepo.save(order);
    }

    public LabOrder getOrderById(Long id) {
        return orderRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("LabOrder","id",id));
    }

    public Page<LabOrder> getDoctorOrders(Long doctorId, int page) {
        return orderRepo.findByDoctorId(doctorId, PageRequest.of(page,15, Sort.by("createdAt").descending()));
    }

    public Page<LabOrder> getAllOrders(int page) {
        return orderRepo.findAll(PageRequest.of(page, 20, Sort.by("createdAt").descending()));
    }

    public Map<String,Object> getDashboardStats() {
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("totalOrders",    orderRepo.count());
        s.put("pendingOrders",  orderRepo.countByStatus("ORDERED"));
        s.put("completedToday", 0); // simplified
        return s;
    }
}
