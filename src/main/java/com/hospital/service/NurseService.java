package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NurseService {

    private final NurseRepository   nurseRepo;
    private final PatientRepository patientRepo;

    // In-memory task store (replace with entity when you add a Task table)
    private static final List<Map<String,Object>> TASK_STORE = new ArrayList<>(Arrays.asList(
        new HashMap<>(Map.of("id",1L,"taskName","Check vitals — Room 101","description","Hourly BP and SpO2 check","priority","HIGH","status","PENDING","dueTime","09:00")),
        new HashMap<>(Map.of("id",2L,"taskName","Medication round","description","Distribute morning medications","priority","MEDIUM","status","PENDING","dueTime","10:00")),
        new HashMap<>(Map.of("id",3L,"taskName","Patient discharge prep — Room 205","description","Prepare discharge summary and patient instructions","priority","LOW","status","PENDING","dueTime","11:00")),
        new HashMap<>(Map.of("id",4L,"taskName","IV line change — Room 108","description","Change IV drip bag and tubing","priority","HIGH","status","IN_PROGRESS","dueTime","08:30")),
        new HashMap<>(Map.of("id",5L,"taskName","Patient education","description","Explain post-op care to Room 302 patient","priority","LOW","status","COMPLETED","dueTime","12:00"))
    ));

    public Map<String,Object> getDashboard(Long userId) {
        Nurse nurse = nurseRepo.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Nurse profile not found"));
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("nurse", nurse);
        s.put("assignedPatients", patientRepo.count());
        s.put("pendingTasks", TASK_STORE.stream().filter(t -> "PENDING".equals(t.get("status"))).count());
        s.put("inProgressTasks", TASK_STORE.stream().filter(t -> "IN_PROGRESS".equals(t.get("status"))).count());
        return s;
    }

    public Page<Patient> getAssignedPatients(Long userId, int page) {
        return patientRepo.findAll(PageRequest.of(page, 15, Sort.by("id").descending()));
    }

    public List<Map<String,Object>> getTasks(Long userId) {
        return Collections.unmodifiableList(TASK_STORE);
    }

    @Transactional
    public Map<String,Object> startTask(Long taskId) {
        TASK_STORE.stream()
            .filter(t -> taskId.equals(((Number) t.get("id")).longValue()))
            .findFirst()
            .ifPresent(t -> {
                if ("PENDING".equals(t.get("status"))) {
                    t.put("status","IN_PROGRESS");
                    t.put("startedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            });
        return Map.of("message","Task started","taskId", taskId);
    }

    @Transactional
    public Map<String,Object> completeTask(Long taskId) {
        TASK_STORE.stream()
            .filter(t -> taskId.equals(((Number) t.get("id")).longValue()))
            .findFirst()
            .ifPresent(t -> {
                t.put("status","COMPLETED");
                t.put("completedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            });
        return Map.of("message","Task completed","taskId", taskId);
    }

    @Transactional
    public Map<String,Object> recordVitals(Map<String,Object> body, Long userId) {
        body.put("recordedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        body.put("recordedByNurseId", userId);
        return Map.of("message","Vitals recorded successfully","data", body);
    }

    public List<Map<String,Object>> getHandoverNotes(Long userId) {
        return List.of(
            Map.of("id",1,"shift","Morning","nurse","Nurse Priya K.","notes","Patient in room 203 needs hourly monitoring — BP elevated","time","07:00","ward","General Ward A"),
            Map.of("id",2,"shift","Morning","nurse","Nurse Rani S.","notes","New admission in room 108 — post-op observation required","time","07:30","ward","Surgical Ward")
        );
    }

    @Transactional
    public Map<String,Object> createHandoverNote(Map<String,Object> body, Long userId) {
        body.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        body.put("nurseId", userId);
        return Map.of("message","Handover note created","data", body);
    }

    public List<Map<String,Object>> getEmarRecords(Long userId) {
        return List.of(
            Map.of("id",1,"medicine","Paracetamol 500mg","patient","John Doe","scheduledTime","08:00","status","PENDING","route","ORAL","dose","1 tablet"),
            Map.of("id",2,"medicine","Amoxicillin 250mg","patient","Jane Smith","scheduledTime","08:30","status","PENDING","route","ORAL","dose","1 capsule"),
            Map.of("id",3,"medicine","Metformin 500mg","patient","Rajesh Kumar","scheduledTime","09:00","status","ADMINISTERED","route","ORAL","dose","1 tablet","administeredAt","09:02")
        );
    }

    @Transactional
    public Map<String,Object> administerMedication(Map<String,Object> body, Long userId) {
        body.put("administeredAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        body.put("administeredByNurseId", userId);
        return Map.of("message","Medication administered","data", body);
    }
}
