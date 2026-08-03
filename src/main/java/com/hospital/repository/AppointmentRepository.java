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

    /* ── by patient ───────────────────────────────────────────── */
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :pid AND a.status IN :statuses")
    List<Appointment> findByPatientIdAndStatusIn(@Param("pid") Long patientId,
                                                  @Param("statuses") List<String> statuses);

    Page<Appointment> findByPatientIdAndStatus(Long patientId, String status, Pageable pageable);

    /* ── by doctor ────────────────────────────────────────────── */
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);

    Page<Appointment> findByDoctorIdAndStatus(Long doctorId, String status, Pageable pageable);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    /* ── by date ──────────────────────────────────────────────── */
    List<Appointment> findByAppointmentDate(LocalDate date);

    Page<Appointment> findByAppointmentDate(LocalDate date, Pageable pageable);

    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate date);

    List<Appointment> findByAppointmentDateAndStatus(LocalDate date, String status);

    /* ── by status ────────────────────────────────────────────── */
    Page<Appointment> findByStatus(String status, Pageable pageable);

    /* ── counts ───────────────────────────────────────────────── */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date")
    long countByAppointmentDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date AND a.status = :status")
    long countByAppointmentDateAndStatus(@Param("date") LocalDate date, @Param("status") String status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :did AND a.appointmentDate = :date")
    long countByDoctorIdAndAppointmentDate(@Param("did") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :did AND a.status = :status")
    long countByDoctorIdAndStatus(@Param("did") Long doctorId, @Param("status") String status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate BETWEEN :start AND :end")
    long countByAppointmentDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status AND a.appointmentDate BETWEEN :start AND :end")
    long countByStatusAndAppointmentDateBetween(@Param("status") String status,
                                                 @Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);

    /* ── misc ─────────────────────────────────────────────────── */
    boolean existsBySlotId(Long slotId);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :start AND :end")
    List<Appointment> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
