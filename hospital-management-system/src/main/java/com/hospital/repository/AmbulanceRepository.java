package com.hospital.repository;

import com.hospital.entity.Ambulance;
import com.hospital.entity.Ambulance.AmbulanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByStatus(AmbulanceStatus status);
    List<Ambulance> findByBranchIdAndIsActiveTrue(Long branchId);
    long countByStatus(AmbulanceStatus status);
    long countByIsActiveTrue();
}
