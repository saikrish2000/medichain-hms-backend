package com.hospital.repository;

import com.hospital.entity.MedicalRecord;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Page<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Long patientId, Pageable p);
    List<MedicalRecord> findByDoctorIdOrderByVisitDateDesc(Long doctorId);
    long countByPatientId(Long patientId);
    long countByDoctorId(Long doctorId);
}
