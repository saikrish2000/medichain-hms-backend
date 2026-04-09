package com.hospital.repository;

import com.hospital.entity.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByStatus(String status);
    List<Ambulance> findByBranchId(Long branchId);
    List<Ambulance> findByIsActiveTrue();
    long countByStatus(String status);
}
