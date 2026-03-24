package com.hospital.repository;

import com.hospital.entity.Bed;
import com.hospital.entity.Bed.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByWardId(Long wardId);
    List<Bed> findByStatus(BedStatus status);
    long countByStatus(BedStatus status);
    long countByWardId(Long wardId);
}
