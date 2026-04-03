package com.hospital.repository;

import com.hospital.entity.BloodInventory;
import com.hospital.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {
    List<BloodInventory> findByBankId(Long bankId);
    Optional<BloodInventory> findByBankIdAndBloodGroup(Long bankId, BloodGroup bloodGroup);
}
