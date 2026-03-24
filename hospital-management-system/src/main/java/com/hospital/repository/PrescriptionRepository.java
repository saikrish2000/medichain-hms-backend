package com.hospital.repository;

import com.hospital.entity.Prescription;
import com.hospital.entity.Prescription.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Page<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);
    Page<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable pageable);
    List<Prescription> findByPatientIdAndStatus(Long patientId, PrescriptionStatus status);
    List<Prescription> findByStatus(PrescriptionStatus status);
    long countByDoctorIdAndStatus(Long doctorId, PrescriptionStatus status);
}
