package com.hospital.repository;

import com.hospital.entity.LabOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    Optional<LabOrder> findByOrderNumber(String orderNumber);
    Page<LabOrder> findByPatientId(Long patientId, Pageable pageable);
    Page<LabOrder> findByDoctorId(Long doctorId, Pageable pageable);
    Page<LabOrder> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
}
