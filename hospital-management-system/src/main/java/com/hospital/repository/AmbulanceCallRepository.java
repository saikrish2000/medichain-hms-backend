package com.hospital.repository;

import com.hospital.entity.AmbulanceCall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmbulanceCallRepository extends JpaRepository<AmbulanceCall, Long> {
    Page<AmbulanceCall> findByStatus(String status, Pageable pageable);
    List<AmbulanceCall> findByAmbulanceIdAndStatusIn(Long ambulanceId, List<String> statuses);
    List<AmbulanceCall> findByStatus(String status);
}
