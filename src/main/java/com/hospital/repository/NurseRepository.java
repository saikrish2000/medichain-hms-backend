package com.hospital.repository;

import com.hospital.entity.Nurse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Long> {

    Optional<Nurse> findByUserId(Long userId);
    Optional<Nurse> findByLicenseNumber(String licenseNumber);
    List<Nurse> findByApprovalStatus(String status);
    Page<Nurse> findByApprovalStatus(String status, Pageable pageable);
    List<Nurse> findByDepartmentId(Long departmentId);
}
