package com.hospital.repository;

import com.hospital.entity.Doctor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Page<Doctor>     findByApprovalStatus(Doctor.ApprovalStatus status, Pageable pageable);
    List<Doctor>     findByApprovalStatus(Doctor.ApprovalStatus status);
    long             countByApprovalStatus(Doctor.ApprovalStatus status);
    List<Doctor>     findBySpecializationId(Long specId);
    List<Doctor>     findBySpecializationIdAndApprovalStatus(Long specId, Doctor.ApprovalStatus status);

    @Query("SELECT d FROM Doctor d WHERE d.specialization.id = :specId " +
           "AND d.approvalStatus = :status AND d.branch.id = :branchId")
    List<Doctor> findBySpecializationIdAndApprovalStatusAndBranchId(
        @Param("specId") Long specId,
        @Param("status") Doctor.ApprovalStatus status,
        @Param("branchId") Long branchId);
}
