package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.Appointment.AppointmentStatus;
import com.hospital.entity.Doctor.ApprovalStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository      doctorRepo;
    private final AppointmentRepository appointmentRepo;
    private final MedicalRecordRepository recordRepo;
    private final PatientRepository     patientRepo;

    // ── DASHBOARD DATA ────────────────────────────────────

    public Map<String, Object> getDashboardData(Long doctorUserId) {
        Doctor doctor = doctorRepo.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", doctorUserId));

        LocalDate today = LocalDate.now();

        long todayCount   = appointmentRepo.countByDoctorIdAndAppointmentDate(doctor.getId(), today);
        long pendingCount = appointmentRepo.countByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.PENDING);
        long totalPatients= appointmentRepo.countDistinctPatientsByDoctorId(doctor.getId());

        // Today's appointments for timeline
        List<Appointment> todayList = appointmentRepo
            .findByDoctorIdAndAppointmentDateOrderByAppointmentTime(doctor.getId(), today);

        // Pending for quick-approve widget
        List<Appointment> pendingAppts = appointmentRepo
            .findByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.PENDING,
                PageRequest.of(0, 5)).getContent();

        // Featured patient: first confirmed today
        Patient featuredPatient = null;
        MedicalRecord featuredRecord = null;
        if (!todayList.isEmpty()) {
            featuredPatient = todayList.get(0).getPatient();
            List<MedicalRecord> records = recordRepo
                .findByPatientIdOrderByVisitDateDesc(featuredPatient.getId(),
                    PageRequest.of(0, 1)).getContent();
            if (!records.isEmpty()) featuredRecord = records.get(0);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("doctorName",       doctor.getUser().getFullName());
        data.put("specialization",   doctor.getSpecialization().getName());
        data.put("department",       doctor.getDepartment().getName());
        data.put("doctor",           doctor);
        data.put("todayAppointments",todayCount);
        data.put("pendingCount",     pendingCount);
        data.put("totalPatients",    totalPatients);
        data.put("todayList",        todayList);
        data.put("pendingAppts",     pendingAppts);
        data.put("featuredPatient",  featuredPatient);
        data.put("featuredRecord",   featuredRecord);
        return data;
    }

    // ── PATIENTS ──────────────────────────────────────────

    public Page<Patient> getDoctorPatients(Long doctorUserId, int page) {
        Doctor doctor = doctorRepo.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", doctorUserId));
        return patientRepo.findByDoctorId(doctor.getId(),
            PageRequest.of(page, 15, Sort.by("id").descending()));
    }

    // ── SEARCH ────────────────────────────────────────────

    public Page<Doctor> search(String q, int page) {
        return doctorRepo.search(q, PageRequest.of(page, 20));
    }

    public Doctor getByUserId(Long userId) {
        return doctorRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", userId));
    }
}
