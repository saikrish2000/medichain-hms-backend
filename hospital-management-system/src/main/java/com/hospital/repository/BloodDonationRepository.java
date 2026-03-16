package com.hospital.repository;

import com.hospital.entity.BloodDonation;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BloodDonationRepository extends JpaRepository<BloodDonation, Long> {
    Page<BloodDonation> findByBloodBankIdOrderByDonationDateDesc(Long bankId, Pageable p);
    List<BloodDonation> findByDonorIdOrderByDonationDateDesc(Long donorId);
    long countByBloodBankIdAndStatus(Long bankId, BloodDonation.DonationStatus status);
}
