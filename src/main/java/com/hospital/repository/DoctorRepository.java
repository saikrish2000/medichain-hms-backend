package com.hospital.repository;

import com.hospital.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    long countByApprovalStatus(String status);
    List<Doctor> findByApprovalStatus(String status);
    Page<Doctor> findByApprovalStatus(String status, Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.specialization.id = :specId AND d.approvalStatus = 'APPROVED'")
    List<Doctor> findApprovedBySpecializationId(@Param("specId") Long specId);

    @Query("SELECT d FROM Doctor d WHERE d.specialization.id = :specId AND d.approvalStatus = 'APPROVED' AND d.branch.id = :branchId")
    List<Doctor> findApprovedBySpecializationIdAndBranchId(@Param("specId") Long specId, @Param("branchId") Long branchId);

    List<Doctor> findBySpecializationIdAndApprovalStatus(Long specId, String approvalStatus);

    @Query("SELECT d FROM Doctor d WHERE d.approvalStatus = 'APPROVED' AND d.isAvailable = true")
    List<Doctor> findAllApprovedAndAvailable();
}
