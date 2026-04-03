package com.hospital.repository;

import com.hospital.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // By doctor
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);
    Page<Appointment> findByDoctorIdAndStatus(Long doctorId, String status, Pageable pageable);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    long countByDoctorIdAndStatus(Long doctorId, String status);

    // By patient
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);
    List<Appointment> findByPatientIdAndStatusIn(Long patientId, List<String> statuses);
    Page<Appointment> findByPatientIdAndStatus(Long patientId, String status, Pageable pageable);

    // By slot
    List<Appointment> findBySlotId(Long slotId);

    // By status
    Page<Appointment> findByStatus(String status, Pageable pageable);

    // Counts
    long countByAppointmentDate(LocalDate date);
    long countByAppointmentDateAndStatus(LocalDate date, String status);
    long countByAppointmentDateBetween(LocalDate from, LocalDate to);
    long countByStatusAndAppointmentDateBetween(String status, LocalDate from, LocalDate to);

    // Date range
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :from AND :to ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    Page<Appointment> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

    // Receptionist
    Page<Appointment> findByAppointmentDate(LocalDate date, Pageable pageable);
    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate date);
}
