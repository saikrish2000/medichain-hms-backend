package com.hospital.repository;

import com.hospital.entity.LabTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    Optional<LabTest> findByTestCode(String testCode);
    Page<LabTest> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT t FROM LabTest t WHERE t.isActive = true AND " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<LabTest> searchByName(@Param("q") String query);

    List<LabTest> findByCategory(LabTest.TestCategory category);
}
