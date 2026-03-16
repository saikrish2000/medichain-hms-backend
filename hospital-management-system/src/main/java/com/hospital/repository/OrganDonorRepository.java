package com.hospital.repository;

import com.hospital.entity.OrganDonor;
import com.hospital.entity.OrganDonor.DonorStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganDonorRepository extends JpaRepository<OrganDonor, Long> {
    Optional<OrganDonor> findByUserId(Long userId);
    Optional<OrganDonor> findByDonorCardNumber(String cardNumber);
    List<OrganDonor>    findByDonorStatus(DonorStatus status);
    Page<OrganDonor>    findByDonorStatus(DonorStatus status, Pageable p);
    Page<OrganDonor>    findAll(Pageable p);
    long countByDonorStatus(DonorStatus status);

    @Query("SELECT d FROM OrganDonor d WHERE d.donorStatus = 'DECEASED' AND d.nextOfKinConsent = true AND d.organsToDonate LIKE %:organ%")
    List<OrganDonor> findAvailableDonorsByOrgan(@Param("organ") String organ);
}
