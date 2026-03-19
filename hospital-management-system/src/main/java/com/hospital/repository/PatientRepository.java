package com.hospital.repository;

import com.hospital.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByPatientIdNumber(String patientIdNumber);

    @Query("SELECT p FROM Patient p JOIN p.user u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.lastName)  LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.patientIdNumber) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Patient> search(@Param("q") String query, Pageable pageable);

    // Patients who have had appointments with a specific doctor
    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    Page<Patient> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);
}
