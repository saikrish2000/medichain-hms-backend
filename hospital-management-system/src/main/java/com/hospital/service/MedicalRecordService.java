package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepo;
    private final PatientRepository       patientRepo;
    private final DoctorRepository        doctorRepo;

    public Page<MedicalRecord> getPatientRecords(Long patientId, int page) {
        return recordRepo.findByPatientIdOrderByVisitDateDesc(patientId, PageRequest.of(page, 15));
    }

    @Transactional
    public MedicalRecord addRecord(MedicalRecord data, Long doctorUserId) {
        Doctor doctor = doctorRepo.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor","userId",doctorUserId));
        data.setDoctor(doctor);
        if (data.getVisitDate() == null)
            data.setVisitDate(java.time.LocalDate.now());
        return recordRepo.save(data);
    }
}
