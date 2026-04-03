package com.hospital.repository;

import com.hospital.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByIsActiveTrue();
    Page<Medicine> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Medicine> searchByKeyword(@Param("q") String q, Pageable pageable);

    List<Medicine> findByQuantityInStockLessThanEqualAndIsActiveTrue(Integer threshold);
}
