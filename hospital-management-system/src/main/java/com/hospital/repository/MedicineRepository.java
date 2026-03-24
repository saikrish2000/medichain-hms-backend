package com.hospital.repository;

import com.hospital.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Page<Medicine> findByIsActiveTrue(Pageable pageable);
    Page<Medicine> findByBranchIdAndIsActiveTrue(Long branchId, Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.isActive = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(m.genericName) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<Medicine> searchByName(@Param("q") String query);

    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity <= m.reorderLevel AND m.isActive = true")
    List<Medicine> findLowStock();

    long countByIsActiveTrue();
}
