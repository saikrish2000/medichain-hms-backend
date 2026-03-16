package com.hospital.repository;

import com.hospital.entity.BloodRequest;
import com.hospital.entity.BloodRequest.RequestStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    Page<BloodRequest> findByStatus(RequestStatus status, Pageable p);
    Page<BloodRequest> findByBloodBankId(Long bankId, Pageable p);
    Page<BloodRequest> findByRequestedById(Long userId, Pageable p);
    List<BloodRequest> findByBloodBankIdAndStatus(Long bankId, RequestStatus status);
    long countByStatus(RequestStatus status);
    long countByBloodBankIdAndStatus(Long bankId, RequestStatus status);
}
