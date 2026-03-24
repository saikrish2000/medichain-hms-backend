package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReceptionistService {

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository     patientRepo;
    private final DoctorRepository      doctorRepo;
    private final InvoiceRepository     invoiceRepo;
    private final WardRepository        wardRepo;
    private final BedRepository         bedRepo;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        stats.put("todayAppointments", appointmentRepo.countByAppointmentDate(today));
        stats.put("availableDoctors",  doctorRepo.countByApprovalStatus(Doctor.ApprovalStatus.APPROVED));
        stats.put("totalPatients",     patientRepo.count());
        stats.put("availableBeds",     bedRepo.countByStatus(Bed.BedStatus.AVAILABLE));
        stats.put("occupiedBeds",      bedRepo.countByStatus(Bed.BedStatus.OCCUPIED));
        stats.put("todayList",
            appointmentRepo.findByAppointmentDate(today,
                PageRequest.of(0, 20, Sort.by("appointmentTime"))).getContent());
        return stats;
    }

    public Page<Appointment> searchAppointments(LocalDate date, int page) {
        if (date == null) date = LocalDate.now();
        return appointmentRepo.findByAppointmentDate(date, PageRequest.of(page, 30, Sort.by("appointmentTime")));
    }

    @Transactional
    public Appointment checkIn(Long appointmentId) {
        Appointment apt = appointmentRepo.findById(appointmentId)
            .orElseThrow(() -> new com.hospital.exception.ResourceNotFoundException("Appointment", "id", appointmentId));
        apt.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        return appointmentRepo.save(apt);
    }
}
