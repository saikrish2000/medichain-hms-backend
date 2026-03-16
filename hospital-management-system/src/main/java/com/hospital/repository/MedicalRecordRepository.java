package com.hospital.repository;

import com.hospital.entity.MedicalRecord;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Page<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Long patientId, Pageable p);
    List<MedicalRecord> findTop5ByPatientIdOrderByVisitDateDesc(Long patientId);
    List<MedicalRecord> findByDoctorIdOrderByVisitDateDesc(Long doctorId);
    long countByPatientId(Long patientId);
}
