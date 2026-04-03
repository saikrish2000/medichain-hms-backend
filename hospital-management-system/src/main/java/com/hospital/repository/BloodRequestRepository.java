package com.hospital.repository;

import com.hospital.entity.BloodRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    Page<BloodRequest> findByStatus(String status, Pageable pageable);
    Page<BloodRequest> findByPatientId(Long patientId, Pageable pageable);
}
