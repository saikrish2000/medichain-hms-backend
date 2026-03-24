package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.Ambulance.AmbulanceStatus;
import com.hospital.entity.AmbulanceCall.CallStatus;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AmbulanceService {

    private final AmbulanceRepository     ambulanceRepo;
    private final AmbulanceCallRepository callRepo;
    private final UserRepository          userRepo;
    private final HospitalBranchRepository branchRepo;

    // ── AMBULANCE MANAGEMENT ───────────────────────────────

    public List<Ambulance> getAvailableAmbulances() {
        return ambulanceRepo.findByStatus(AmbulanceStatus.AVAILABLE);
    }

    public List<Ambulance> getAllAmbulances() {
        return ambulanceRepo.findAll();
    }

    @Transactional
    public Ambulance saveAmbulance(Ambulance ambulance) {
        return ambulanceRepo.save(ambulance);
    }

    @Transactional
    public void updateLocation(Long ambulanceId, Double lat, Double lng) {
        ambulanceRepo.findById(ambulanceId).ifPresent(a -> {
            a.setCurrentLatitude(lat);
            a.setCurrentLongitude(lng);
            a.setLastLocationUpdate(LocalDateTime.now());
            ambulanceRepo.save(a);
        });
    }

    // ── CALL MANAGEMENT ────────────────────────────────────

    @Transactional
    public AmbulanceCall requestAmbulance(String callerName, String callerPhone,
                                           String pickupAddress, Double lat, Double lng,
                                           String emergencyType, Long patientId) {
        List<Ambulance> available = ambulanceRepo.findByStatus(AmbulanceStatus.AVAILABLE);

        AmbulanceCall call = AmbulanceCall.builder()
            .callerName(callerName)
            .callerPhone(callerPhone)
            .pickupAddress(pickupAddress)
            .pickupLatitude(lat)
            .pickupLongitude(lng)
            .emergencyType(emergencyType)
            .status(CallStatus.REQUESTED)
            .build();

        if (patientId != null) {
            // attach patient if known
        }

        // Auto-dispatch if ambulance available
        if (!available.isEmpty()) {
            Ambulance ambulance = available.get(0);
            ambulance.setStatus(AmbulanceStatus.DISPATCHED);
            ambulanceRepo.save(ambulance);
            call.setAmbulance(ambulance);
            call.setStatus(CallStatus.DISPATCHED);
            call.setDispatchedAt(LocalDateTime.now());
        }

        return callRepo.save(call);
    }

    @Transactional
    public AmbulanceCall updateCallStatus(Long callId, CallStatus newStatus) {
        AmbulanceCall call = callRepo.findById(callId)
            .orElseThrow(() -> new ResourceNotFoundException("AmbulanceCall", "id", callId));
        call.setStatus(newStatus);
        if (newStatus == CallStatus.AT_SCENE) call.setArrivedAt(LocalDateTime.now());
        if (newStatus == CallStatus.COMPLETED) {
            call.setCompletedAt(LocalDateTime.now());
            if (call.getAmbulance() != null) {
                call.getAmbulance().setStatus(AmbulanceStatus.AVAILABLE);
                ambulanceRepo.save(call.getAmbulance());
            }
        }
        return callRepo.save(call);
    }

    public Page<AmbulanceCall> getAllCalls(int page) {
        return callRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, 20));
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalAmbulances",  ambulanceRepo.countByIsActiveTrue());
        stats.put("available",        ambulanceRepo.countByStatus(AmbulanceStatus.AVAILABLE));
        stats.put("dispatched",       ambulanceRepo.countByStatus(AmbulanceStatus.DISPATCHED));
        stats.put("activeCalls",      callRepo.countByStatus(CallStatus.DISPATCHED) +
                                      callRepo.countByStatus(CallStatus.ON_ROUTE));
        stats.put("ambulances",       ambulanceRepo.findAll());
        stats.put("recentCalls",      callRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(0,10)).getContent());
        return stats;
    }
}
