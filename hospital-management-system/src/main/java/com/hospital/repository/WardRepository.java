package com.hospital.repository;

import com.hospital.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByBranchIdAndIsActiveTrue(Long branchId);
    List<Ward> findByIsActiveTrue();

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.ward.branch.id = :branchId AND b.status = 'AVAILABLE'")
    long countAvailableBeds(Long branchId);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.status = 'OCCUPIED'")
    long countOccupiedBeds();

    @Query("SELECT COUNT(b) FROM Bed b")
    long countAllBeds();
}
