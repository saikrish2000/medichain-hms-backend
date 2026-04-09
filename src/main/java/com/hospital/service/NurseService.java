package com.hospital.service;

import com.hospital.entity.*;
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
public class NurseService {

    private final NurseRepository   nurseRepo;
    private final PatientRepository patientRepo;

    public Map<String,Object> getDashboard(Long userId) {
        Nurse nurse = nurseRepo.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Nurse profile not found"));
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("nurse", nurse);
        s.put("assignedPatients", patientRepo.count());
        return s;
    }

    public Page<Patient> getAssignedPatients(Long userId, int page) {
        return patientRepo.findAll(PageRequest.of(page, 15));
    }

    public List<Map<String,Object>> getTasks(Long userId) {
        return List.of(Map.of("id",1,"task","Check vitals — Room 101","priority","HIGH","due","09:00"),
                       Map.of("id",2,"task","Medication round","priority","MEDIUM","due","10:00"),
                       Map.of("id",3,"task","Patient discharge prep — Room 205","priority","LOW","due","11:00"));
    }

    @Transactional
    public Map<String,Object> completeTask(Long taskId) {
        return Map.of("message","Task "+taskId+" completed");
    }

    @Transactional
    public Map<String,Object> recordVitals(Map<String,Object> body, Long userId) {
        return Map.of("message","Vitals recorded","data",body);
    }

    public List<Map<String,Object>> getHandoverNotes(Long userId) {
        return List.of(Map.of("id",1,"shift","Morning","nurse","Nurse Priya","notes","Patient in room 203 needs hourly monitoring","time","07:00"));
    }

    @Transactional
    public Map<String,Object> createHandoverNote(Map<String,Object> body, Long userId) {
        return Map.of("message","Handover note created","data",body);
    }

    public List<Map<String,Object>> getEmarRecords(Long userId) {
        return List.of(Map.of("id",1,"medicine","Paracetamol 500mg","patient","John Doe","time","08:00","status","PENDING"),
                       Map.of("id",2,"medicine","Amoxicillin 250mg","patient","Jane Smith","time","08:30","status","PENDING"));
    }

    @Transactional
    public Map<String,Object> administerMedication(Map<String,Object> body, Long userId) {
        return Map.of("message","Medication administered","data",body);
    }
}
