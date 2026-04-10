package com.hospital.repository;

import com.hospital.entity.OrganRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrganRequestRepository extends JpaRepository<OrganRequest, Long> {

    Page<OrganRequest> findByPatientId(Long patientId, Pageable pageable);

    List<OrganRequest> findByStatus(String status);

    Page<OrganRequest> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COUNT(r) FROM OrganRequest r WHERE r.status NOT IN ('COMPLETED','CANCELLED')")
    long countActiveRequests();
}
