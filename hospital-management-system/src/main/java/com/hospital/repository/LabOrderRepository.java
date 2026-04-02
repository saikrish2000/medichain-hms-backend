package com.hospital.repository;

import com.hospital.entity.LabOrder;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    Page<LabOrder> findByDoctorId(Long doctorId, Pageable pageable);
    long countByStatus(LabOrder.OrderStatus status);
}
