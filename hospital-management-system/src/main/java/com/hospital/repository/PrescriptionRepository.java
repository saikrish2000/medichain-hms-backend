package com.hospital.repository;

import com.hospital.entity.Prescription;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Page<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable pageable);
    Page<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);
    Page<Prescription> findByStatus(Prescription.Status status, Pageable pageable);
    long countByPatientId(Long patientId);
    long countByStatus(Prescription.Status status);
}
