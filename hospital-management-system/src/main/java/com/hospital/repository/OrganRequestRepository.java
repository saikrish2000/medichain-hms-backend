package com.hospital.repository;

import com.hospital.entity.OrganRequest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrganRequestRepository extends JpaRepository<OrganRequest, Long> {
    Page<OrganRequest>  findByStatus(OrganRequest.RequestStatus status, Pageable p);
    List<OrganRequest>  findByPatientIdOrderByCreatedAtDesc(Long patientId);
    long countByStatus(OrganRequest.RequestStatus status);
}
