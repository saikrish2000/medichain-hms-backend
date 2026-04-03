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
            a.setCurrentLatitude(lat);
            a.setCurrentLongitude(lng);
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
        call.setRequestTime(LocalDateTime.now());
        call.setStatus(CallStatus.REQUESTED);
        if (!available.isEmpty()) {
            Ambulance amb = available.get(0);
            amb.setStatus(AmbulanceStatus.DISPATCHED);
            ambulanceRepo.save(amb);
            call.setAmbulance(amb);
            call.setStatus("DISPATCHED");
            call.setDispatchedAt(LocalDateTime.now());
        }
        return callRepo.save(call);
    }

    @Transactional
    public AmbulanceCall updateCallStatus(Long callId, CallStatus newStatus) {
        AmbulanceCall call = callRepo.findById(callId)
            .orElseThrow(() -> new ResourceNotFoundException("AmbulanceCall","id",callId));
        call.setStatus(newStatus);
        if (newStatus == CallStatus.AT_SCENE)  call.setArrivedAt(LocalDateTime.now());
        if (newStatus == "COMPLETED") {
            call.setCompletedAt(LocalDateTime.now());
            if (call.getAmbulance() != null) {
                call.getAmbulance().setStatus("AVAILABLE");
                ambulanceRepo.save(call.getAmbulance());
            }
        }
        return callRepo.save(call);
    }

    public Page<AmbulanceCall> getAllCalls(int page) {
        return callRepo.findAllByOrderByRequestTimeDesc(PageRequest.of(page, 20));
    }

    public Map<String,Object> getDashboardStats() {
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("totalAmbulances", ambulanceRepo.count());
        stats.put("available",       ambulanceRepo.countByStatus("AVAILABLE"));
        stats.put("dispatched",      ambulanceRepo.countByStatus(AmbulanceStatus.DISPATCHED));
        stats.put("activeCalls",     callRepo.countByStatus("DISPATCHED") +
                                     callRepo.countByStatus(CallStatus.ON_ROUTE));
        stats.put("recentCalls",     callRepo.findAllByOrderByRequestTimeDesc(PageRequest.of(0,5)).getContent());
        return stats;
    }
}
