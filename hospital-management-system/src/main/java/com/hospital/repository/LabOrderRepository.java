package com.hospital.repository;

import com.hospital.entity.LabOrder;
import com.hospital.entity.LabOrder.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    Page<LabOrder> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);
    Page<LabOrder> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable pageable);
    Page<LabOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);
    List<LabOrder> findByStatus(OrderStatus status);
    long countByStatus(OrderStatus status);
}
