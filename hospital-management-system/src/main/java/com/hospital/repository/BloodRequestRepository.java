package com.hospital.repository;

import com.hospital.entity.BloodRequest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    long countByStatus(BloodRequest.RequestStatus status);
    Page<BloodRequest> findByStatus(BloodRequest.RequestStatus status, Pageable pageable);
}
