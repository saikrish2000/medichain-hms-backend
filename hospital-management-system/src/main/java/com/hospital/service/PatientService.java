package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository     patientRepo;
    private final AppointmentRepository appointmentRepo;
    private final InvoiceRepository     invoiceRepo;
    private final MedicalRecordRepository medRecordRepo;

    public Long getPatientIdByUserId(Long userId) {
        return patientRepo.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Patient profile not found")).getId();
    }

    public Patient getProfile(Long userId) {
        return patientRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient","userId",userId));
    }

    @Transactional
    public Patient updateProfile(Long userId, Patient data) {
        Patient existing = patientRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient","userId",userId));
        if (data.getBloodGroup()   != null) existing.setBloodGroup(data.getBloodGroup());
        if (data.getDateOfBirth()  != null) existing.setDateOfBirth(data.getDateOfBirth());
        if (data.getAddress()      != null) existing.setAddress(data.getAddress());
        if (data.getEmergencyContact() != null) existing.setEmergencyContact(data.getEmergencyContact());
        return patientRepo.save(existing);
    }

    public Map<String,Object> getDashboard(Long userId) {
        Patient patient = patientRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient","userId",userId));
        Long pid = patient.getId();
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("patient", patient);
        stats.put("upcomingAppointments", appointmentRepo.findNextAppointments(pid, PageRequest.of(0,3)));
        stats.put("pendingBills",        invoiceRepo.countByPatientIdAndStatus(pid, Invoice.PaymentStatus.PENDING));
        stats.put("totalVisits",         medRecordRepo.countByPatientId(pid));
        stats.put("recentRecords",       medRecordRepo.findByPatientIdOrderByVisitDateDesc(pid, PageRequest.of(0,3)).getContent());
        return stats;
    }

    public List<MedicalRecord> getVitals(Long patientId) {
        return medRecordRepo.findByPatientIdOrderByVisitDateDesc(
            patientId, PageRequest.of(0,10)).getContent();
    }
}
