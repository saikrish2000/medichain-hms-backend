package com.hospital.repository;

import com.hospital.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Page<MedicalRecord> findByPatientId(Long patientId, Pageable pageable);
    Page<MedicalRecord> findByDoctorId(Long doctorId, Pageable pageable);
    List<MedicalRecord> findByPatientIdOrderByRecordDateDesc(Long patientId);
    List<MedicalRecord> findByAppointmentId(Long appointmentId);
}
