package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepo;
    private final PatientRepository       patientRepo;
    private final DoctorRepository        doctorRepo;

    public Page<MedicalRecord> getPatientRecords(Long patientId, int page) {
        return recordRepo.findByPatientId(patientId, PageRequest.of(page, 15, Sort.by("recordDate").descending()));
    }

    public List<MedicalRecord> getPatientRecordsList(Long userId) {
        Patient patient = patientRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "userId", userId));
        return recordRepo.findByPatientIdOrderByRecordDateDesc(patient.getId());
    }

    public List<MedicalRecord> getPatientVitals(Long userId) {
        return getPatientRecordsList(userId).stream().limit(10).toList();
    }

    @Transactional
    public MedicalRecord addRecord(MedicalRecord data, Long doctorUserId) {
        Doctor doctor = doctorRepo.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", doctorUserId));
        data.setDoctor(doctor);
        if (data.getRecordDate() == null)
            data.setRecordDate(java.time.LocalDate.now());
        return recordRepo.save(data);
    }

    public MedicalRecord getById(Long id) {
        return recordRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));
    }
}
