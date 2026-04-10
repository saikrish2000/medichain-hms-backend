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

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByStatus(String status, Pageable pageable);

    List<Appointment> findByAppointmentDate(LocalDate date);

    List<Appointment> findByAppointmentDateAndStatus(LocalDate date, String status);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date")
    long countByAppointmentDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date")
    long countByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    boolean existsBySlotId(Long slotId);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);

    Page<Appointment> findByPatientIdAndStatus(Long patientId, String status, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :start AND :end")
    List<Appointment> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
