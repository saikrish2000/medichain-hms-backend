package com.hospital.repository;

import com.hospital.entity.OrganDonor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganDonorRepository extends JpaRepository<OrganDonor, Long> {
    Optional<OrganDonor> findByUserId(Long userId);
    Page<OrganDonor> findByStatus(String status, Pageable pageable);
}
