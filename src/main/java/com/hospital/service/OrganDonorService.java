package com.hospital.service;

import com.hospital.entity.OrganDonor;
import com.hospital.entity.OrganRequest;
import com.hospital.entity.User;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class OrganDonorService {

    private final OrganDonorRepository  donorRepo;
    private final OrganRequestRepository requestRepo;
    private final UserRepository         userRepo;

    @Transactional
    public OrganDonor registerDonor(Long userId, Map<String,Object> data) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User","id",userId));
        OrganDonor d = new OrganDonor();
        d.setUser(user);
        d.setOrgansToDonate((String) data.get("organsToDonate"));
        d.setMedicalConditions((String) data.get("medicalConditions"));
        d.setConsentDocumentUrl((String) data.get("consentDocumentUrl"));
        d.setStatus("REGISTERED");
        return donorRepo.save(d);
    }

    public Page<OrganDonor> getAllDonors(int page) {
        return donorRepo.findAll(PageRequest.of(page, 20, Sort.by("createdAt").descending()));
    }

    public OrganDonor getDonorById(Long id) {
        return donorRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("OrganDonor","id",id));
    }

    @Transactional
    public OrganRequest createRequest(Map<String,Object> data) {
        OrganRequest r = new OrganRequest();
        r.setOrganNeeded((String) data.get("organNeeded"));
        r.setUrgencyLevel((String) data.getOrDefault("urgencyLevel","HIGH"));
        r.setMedicalJustification((String) data.get("medicalJustification"));
        r.setStatus("WAITING");
        return requestRepo.save(r);
    }

    public Page<OrganRequest> getAllRequests(int page) {
        return requestRepo.findAll(PageRequest.of(page, 20, Sort.by("createdAt").descending()));
    }

    @Transactional
    public OrganRequest updateRequestStatus(Long id, String status) {
        OrganRequest r = requestRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("OrganRequest","id",id));
        r.setStatus(status);
        return requestRepo.save(r);
    }

    public Map<String,Object> getDashboardStats() {
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("totalDonors", donorRepo.count());
        s.put("totalRequests", requestRepo.count());
        s.put("waitingRequests", requestRepo.countByStatus("WAITING"));
        s.put("matchedRequests", requestRepo.countByStatus("MATCHED"));
        return s;
    }
}
