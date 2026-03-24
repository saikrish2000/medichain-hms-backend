package com.hospital.repository;

import com.hospital.entity.AmbulanceCall;
import com.hospital.entity.AmbulanceCall.CallStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmbulanceCallRepository extends JpaRepository<AmbulanceCall, Long> {
    Page<AmbulanceCall> findByStatusOrderByCreatedAtDesc(CallStatus status, Pageable pageable);
    List<AmbulanceCall> findByAmbulanceIdAndStatusIn(Long ambulanceId, List<CallStatus> statuses);
    Page<AmbulanceCall> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatus(CallStatus status);
}
