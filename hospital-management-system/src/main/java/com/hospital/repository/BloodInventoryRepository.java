package com.hospital.repository;

import com.hospital.entity.BloodInventory;
import com.hospital.enums.BloodGroup;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {
    Optional<BloodInventory> findByBloodBankIdAndBloodGroup(Long bankId, BloodGroup group);
    List<BloodInventory> findByBloodBankId(Long bankId);

    @Query("SELECT COALESCE(SUM(i.unitsAvailable),0) FROM BloodInventory i")
    Optional<Long> sumTotalUnits();
}
