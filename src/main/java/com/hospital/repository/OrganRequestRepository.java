package com.hospital.repository;

import com.hospital.entity.OrganRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganRequestRepository extends JpaRepository<OrganRequest, Long> {
    Page<OrganRequest> findByPatientId(Long patientId, Pageable pageable);
    Page<OrganRequest> findByStatus(String status, Pageable pageable);
}
