package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReceptionistService {

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository     patientRepo;

    @Transactional
    public Appointment checkIn(Long appointmentId) {
        Appointment appt = appointmentRepo.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment","id",appointmentId));
        appt.setStatus("IN_PROGRESS");
        appt.setCheckedInAt(LocalDateTime.now());
        return appointmentRepo.save(appt);
    }

    public List<Appointment> getTodayAppointments() {
        return appointmentRepo.findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate.now());
    }

    public Page<Appointment> getAppointmentsByDate(LocalDate date, int page) {
        return appointmentRepo.findByAppointmentDate(date, PageRequest.of(page, 30));
    }

    public List<Patient> searchPatients(String q) {
        return patientRepo.searchByKeyword(q, PageRequest.of(0, 10)).getContent();
    }

    public Map<String,Object> getDashboardStats() {
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("todayAppointments",    appointmentRepo.countByAppointmentDate(LocalDate.now()));
        s.put("checkedIn",            appointmentRepo.countByAppointmentDateAndStatus(LocalDate.now(), "IN_PROGRESS"));
        s.put("pendingToday",         appointmentRepo.countByAppointmentDateAndStatus(LocalDate.now(), "PENDING"));
        s.put("upcomingAppointments", getTodayAppointments());
        return s;
    }
}
