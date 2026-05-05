package com.hospital.repository;

import com.hospital.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);
    Page<Prescription> findByDoctorId(Long doctorId, Pageable pageable);
    List<Prescription> findByPatientIdAndStatus(Long patientId, String status);
    List<Prescription> findByStatus(String status);
    Page<Prescription> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
}
