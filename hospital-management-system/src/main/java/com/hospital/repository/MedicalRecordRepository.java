package com.hospital.repository;

import com.hospital.entity.MedicalRecord;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Page<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Long patientId, Pageable pageable);
    Page<MedicalRecord> findByDoctorIdOrderByVisitDateDesc(Long doctorId, Pageable pageable);
    long countByPatientId(Long patientId);
}
