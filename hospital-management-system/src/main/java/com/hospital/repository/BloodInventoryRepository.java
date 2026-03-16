package com.hospital.repository;

import com.hospital.entity.BloodInventory;
import com.hospital.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {

    List<BloodInventory> findByBloodBankIdOrderByBloodGroup(Long bankId);

    Optional<BloodInventory> findByBloodBankIdAndBloodGroup(Long bankId, BloodGroup group);

    @Query("SELECT i FROM BloodInventory i WHERE i.bloodBank.isActive = true ORDER BY i.bloodGroup")
    List<BloodInventory> findAllActive();

    @Query("SELECT i FROM BloodInventory i WHERE i.unitsAvailable <= i.minimumThreshold AND i.bloodBank.isActive = true")
    List<BloodInventory> findLowStock();
}
