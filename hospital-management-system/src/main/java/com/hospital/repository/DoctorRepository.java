package com.hospital.repository;

import com.hospital.entity.Doctor;
import com.hospital.entity.Doctor.ApprovalStatus;
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
    List<Doctor>  findByApprovalStatus(ApprovalStatus status);
    Page<Doctor>  findByApprovalStatus(ApprovalStatus status, Pageable pageable);
    List<Doctor>  findByDepartmentId(Long departmentId);
    List<Doctor>  findBySpecializationId(Long specializationId);
    long countByApprovalStatus(ApprovalStatus status);

    @Query("SELECT d FROM Doctor d WHERE d.approvalStatus = 'APPROVED' " +
           "AND d.isAvailable = true AND d.department.branch.id = :branchId")
    List<Doctor> findAvailableByBranch(@Param("branchId") Long branchId);

    @Query("SELECT d FROM Doctor d WHERE d.specialization.id = :specId " +
           "AND d.approvalStatus = :status AND d.department.branch.id = :branchId")
    List<Doctor> findBySpecializationIdAndApprovalStatusAndBranchId(
        @Param("specId") Long specId,
        @Param("status") ApprovalStatus status,
        @Param("branchId") Long branchId);

    @Query("SELECT d FROM Doctor d JOIN d.user u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.lastName)  LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(d.specialization.name) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Doctor> search(@Param("q") String query, Pageable pageable);
}
