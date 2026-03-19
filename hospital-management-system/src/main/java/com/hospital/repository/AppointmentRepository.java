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

    // ── BY DOCTOR ──────────────────────────────────────────
    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTime(
        Long doctorId, LocalDate date);

    Page<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status, Pageable p);

    Page<Appointment> findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(
        Long doctorId, Pageable p);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    long countByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :docId")
    long countDistinctPatientsByDoctorId(@Param("docId") Long doctorId);

    // ── BY PATIENT ─────────────────────────────────────────
    Page<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(
        Long patientId, Pageable p);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);

    // ── BY SLOT ────────────────────────────────────────────
    boolean existsBySlotIdAndStatusNot(Long slotId, AppointmentStatus status);

    long countBySlotIdAndStatus(Long slotId, AppointmentStatus status);

    // ── GENERAL ────────────────────────────────────────────
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date ORDER BY a.appointmentTime")
    List<Appointment> findByDate(@Param("date") LocalDate date);

    long countByAppointmentDate(LocalDate date);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable p);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :from AND :to ORDER BY a.appointmentDate")
    List<Appointment> findBetweenDates(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
