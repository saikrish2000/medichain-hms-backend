package com.hospital.repository;

import com.hospital.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    List<DoctorSlot> findByDoctorId(Long doctorId);

    List<DoctorSlot> findByDoctorIdAndSlotDate(Long doctorId, LocalDate slotDate);

    @Query("SELECT s FROM DoctorSlot s WHERE s.doctor.id = :doctorId " +
           "AND s.slotDate = :date AND s.isBlocked = false AND s.isActive = true " +
           "AND s.currentPatients < s.maxPatients ORDER BY s.startTime ASC")
    List<DoctorSlot> findAvailableSlots(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    List<DoctorSlot> findByDoctorIdAndIsRecurringTrue(Long doctorId);
}
