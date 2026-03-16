package com.hospital.repository;

import com.hospital.entity.DoctorSlot;
import com.hospital.entity.DoctorSlot.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    List<DoctorSlot> findByDoctorIdAndIsActiveOrderByStartTime(Long doctorId, Boolean active);

    List<DoctorSlot> findByDoctorIdAndSlotDateOrderByStartTime(Long doctorId, LocalDate date);

    List<DoctorSlot> findByDoctorIdAndDayOfWeekAndSlotTypeOrderByStartTime(
            Long doctorId, DayOfWeek dayOfWeek, DoctorSlot.SlotType slotType);

    @Query("""
        SELECT s FROM DoctorSlot s
        WHERE s.doctor.id = :doctorId
          AND s.isActive = true
          AND s.status = 'AVAILABLE'
          AND (
            (s.slotType = 'SPECIFIC_DATE' AND s.slotDate = :date)
            OR
            (s.slotType = 'RECURRING' AND s.dayOfWeek = :dow)
          )
        ORDER BY s.startTime
        """)
    List<DoctorSlot> findAvailableSlots(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("dow") DayOfWeek dow);

    @Query("""
        SELECT s FROM DoctorSlot s
        WHERE s.doctor.id = :doctorId
          AND s.isActive = true
          AND (
            (s.slotType = 'SPECIFIC_DATE' AND s.slotDate BETWEEN :from AND :to)
            OR s.slotType = 'RECURRING'
          )
        ORDER BY s.slotDate, s.startTime
        """)
    List<DoctorSlot> findSlotsForWeek(
            @Param("doctorId") Long doctorId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate date,
            java.time.LocalTime startTime);
}
