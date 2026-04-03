package com.hospital.repository;

import com.hospital.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByWardId(Long wardId);
    List<Bed> findByStatus(String status);
    List<Bed> findByPatientId(Long patientId);
    long countByWardIdAndStatus(Long wardId, String status);
}
