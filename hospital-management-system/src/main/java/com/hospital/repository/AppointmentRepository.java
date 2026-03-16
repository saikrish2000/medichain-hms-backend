package com.hospital.repository;

import com.hospital.entity.Appointment;
import com.hospital.entity.Appointment.AppointmentStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Doctor's appointments
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTime(
            Long doctorId, LocalDate date);

    Page<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status, Pageable p);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable p);

    // Patient's appointments
    Page<Appointment> findByPatientId(Long patientId, Pageable p);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    // Count helpers
    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    long countByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    long countByStatus(AppointmentStatus status);

    // Slot conflict check
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId, LocalDate date,
            java.time.LocalTime time, AppointmentStatus status);

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDate BETWEEN :from AND :to
        ORDER BY a.appointmentDate, a.appointmentTime
        """)
    List<Appointment> findByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.status = 'PENDING'
        ORDER BY a.createdAt DESC
        """)
    Page<Appointment> findAllPending(Pageable p);
}
