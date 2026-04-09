package com.hospital.service;

import com.hospital.entity.*;
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

    public Ambulance saveAmbulance(Ambulance ambulance) { return ambulanceRepo.save(ambulance); }

    public List<Ambulance> getAllAmbulances() { return ambulanceRepo.findAll(); }

    @Transactional
    public void updateLocation(Long id, Double lat, Double lng) {
        ambulanceRepo.findById(id).ifPresent(a -> {
            a.setCurrentLatitude(lat != null ? java.math.BigDecimal.valueOf(lat) : null);
            a.setCurrentLongitude(lng != null ? java.math.BigDecimal.valueOf(lng) : null);
            a.setLastLocationUpdate(LocalDateTime.now());
            ambulanceRepo.save(a);
        });
    }

    @Transactional
    public AmbulanceCall requestAmbulance(String callerName, String callerPhone,
                                          String pickupAddress, String emergencyType) {
        List<Ambulance> available = ambulanceRepo.findByStatus("AVAILABLE");
        AmbulanceCall call = new AmbulanceCall();
        call.setCallerName(callerName);
        call.setCallerPhone(callerPhone);
        call.setPickupAddress(pickupAddress);
        call.setEmergencyType(emergencyType);
        call.setCreatedAt(LocalDateTime.now());
        call.setStatus("PENDING");
        if (!available.isEmpty()) {
            Ambulance amb = available.get(0);
            amb.setStatus("DISPATCHED");
            ambulanceRepo.save(amb);
            call.setAmbulance(amb);
            call.setStatus("DISPATCHED");
            call.setDispatchedAt(LocalDateTime.now());
        }
        return callRepo.save(call);
    }

    @Transactional
    public AmbulanceCall updateCallStatus(Long callId, String newStatus) {
        AmbulanceCall call = callRepo.findById(callId)
            .orElseThrow(() -> new ResourceNotFoundException("AmbulanceCall","id",callId));
        call.setStatus(newStatus);
        if ("AT_SCENE".equals(newStatus))  call.setArrivedAt(LocalDateTime.now());
        if ("COMPLETED".equals(newStatus)) {
            call.setCompletedAt(LocalDateTime.now());
            if (call.getAmbulance() != null) {
                call.getAmbulance().setStatus("AVAILABLE");
                ambulanceRepo.save(call.getAmbulance());
            }
        }
        return callRepo.save(call);
    }

    public Page<AmbulanceCall> getAllCalls(int page) {
        return callRepo.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public Map<String,Object> getDashboardStats() {
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("totalAmbulances", ambulanceRepo.count());
        stats.put("available",       ambulanceRepo.countByStatus("AVAILABLE"));
        stats.put("dispatched",      ambulanceRepo.countByStatus("DISPATCHED"));
        stats.put("activeCalls",     callRepo.countByStatus("DISPATCHED") +
                                     callRepo.countByStatus("ON_ROUTE"));
        stats.put("recentCalls",     callRepo.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent());
        return stats;
    }
}
