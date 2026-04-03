package com.hospital.repository;

import com.hospital.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByPatientIdNumber(String patientIdNumber);

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.user.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.user.lastName)  LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.user.email)     LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "p.user.phone            LIKE CONCAT('%',:q,'%')")
    Page<Patient> searchByKeyword(@Param("q") String q, Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT DISTINCT p FROM Patient p JOIN Appointment a ON a.patient.id = p.id WHERE a.doctor.id = :doctorId")
    Page<Patient> findPatientsByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);
}
