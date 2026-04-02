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

    Page<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status, Pageable pageable);
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    List<Appointment> findByDoctorIdAndAppointmentDateOrderByAppointmentTime(Long doctorId, LocalDate date);
    List<Appointment> findByAppointmentDateOrderByAppointmentTime(LocalDate date);
    Page<Appointment> findByAppointmentDate(LocalDate date, Pageable pageable);

    long countByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    long countByAppointmentDate(LocalDate date);
    long countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :doctorId")
    long countDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.appointmentDate >= CURRENT_DATE ORDER BY a.appointmentDate ASC")
    List<Appointment> findNextAppointments(@Param("patientId") Long patientId, Pageable pageable);
}
