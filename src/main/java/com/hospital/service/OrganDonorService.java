package com.hospital.service;

import com.hospital.entity.OrganDonor;
import com.hospital.entity.OrganRequest;
import com.hospital.entity.User;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.OrganDonorRepository;
import com.hospital.repository.OrganRequestRepository;
import com.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrganDonorService {

    private final OrganDonorRepository  donorRepo;
    private final OrganRequestRepository requestRepo;
    private final UserRepository         userRepo;

    @Transactional
    public OrganDonor registerDonor(Long userId, Map<String, Object> data) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        OrganDonor donor = new OrganDonor();
        donor.setUser(user);
        donor.setOrgansToDonate((String) data.get("organsToDonate"));
        donor.setMedicalConditions((String) data.get("medicalConditions"));
        donor.setConsentDocumentUrl((String) data.get("consentDocumentUrl"));
        donor.setStatus("REGISTERED");
        return donorRepo.save(donor);
    }

    public Page<OrganDonor> getAllDonors(int page) {
        return donorRepo.findAll(PageRequest.of(page, 20, Sort.by("createdAt").descending()));
    }

    public OrganDonor getDonorById(Long id) {
        return donorRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrganDonor", "id", id));
    }

    @Transactional
    public OrganRequest createRequest(Map<String, Object> data) {
        OrganRequest req = new OrganRequest();
        req.setOrganNeeded((String) data.get("organNeeded"));
        req.setUrgencyLevel((String) data.getOrDefault("urgencyLevel", "HIGH"));
        req.setMedicalJustification((String) data.get("medicalJustification"));
        req.setStatus("WAITING");
        return requestRepo.save(req);
    }

    public Page<OrganRequest> getAllRequests(int page) {
        return requestRepo.findAll(PageRequest.of(page, 20, Sort.by("createdAt").descending()));
    }

    @Transactional
    public OrganRequest updateRequestStatus(Long id, String status) {
        OrganRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrganRequest", "id", id));
        req.setStatus(status);
        return requestRepo.save(req);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalDonors",     donorRepo.count());
        stats.put("totalRequests",   requestRepo.count());
        stats.put("waitingRequests", requestRepo.countByStatus("WAITING"));
        stats.put("matchedRequests", requestRepo.countByStatus("MATCHED"));
        return stats;
    }
}
