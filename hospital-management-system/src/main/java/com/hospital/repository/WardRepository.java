package com.hospital.repository;

import com.hospital.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByBranchId(Long branchId);
    List<Ward> findByDepartmentId(Long departmentId);
    List<Ward> findByIsActiveTrue();
}
