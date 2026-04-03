package com.hospital.repository;

import com.hospital.entity.BloodDonation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodDonationRepository extends JpaRepository<BloodDonation, Long> {
    Page<BloodDonation> findByBankId(Long bankId, Pageable pageable);
    Page<BloodDonation> findByDonorId(Long donorId, Pageable pageable);
    Page<BloodDonation> findByStatus(String status, Pageable pageable);
}
