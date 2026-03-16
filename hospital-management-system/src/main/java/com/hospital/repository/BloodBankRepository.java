package com.hospital.repository;

import com.hospital.entity.BloodBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodBankRepository extends JpaRepository<BloodBank, Long> {
    List<BloodBank>   findByIsActive(Boolean active);
    List<BloodBank>   findByBranchId(Long branchId);
    Optional<BloodBank> findByBranchIdAndIsActive(Long branchId, Boolean active);
}
