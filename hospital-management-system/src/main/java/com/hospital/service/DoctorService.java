package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository      doctorRepo;
    private final PatientRepository     patientRepo;
    private final AppointmentRepository appointmentRepo;
    private final DoctorSlotRepository  slotRepo;
    private final SpecializationRepository specRepo;
    private final HospitalBranchRepository branchRepo;

    public Long getDoctorIdByUserId(Long userId) {
        return doctorRepo.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Doctor profile not found"))
            .getId();
    }

    public Map<String,Object> getDashboard(Long userId) {
        Doctor doctor = doctorRepo.findByUserId(userId)
            .orElseThrow(() -> new BadRequestException("Doctor profile not found"));
        Long docId = doctor.getId();
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("doctor", doctor);
        stats.put("todayAppointments",     appointmentRepo.countByDoctorIdAndAppointmentDate(docId, LocalDate.now()));
        stats.put("pendingAppointments",   appointmentRepo.countByDoctorIdAndStatus(docId, "PENDING"));
        stats.put("totalPatients",         (long) appointmentRepo.findByDoctorId(docId, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
        stats.put("todaySchedule",         appointmentRepo.findByDoctorIdAndAppointmentDate(docId, LocalDate.now()));
        return stats;
    }

    public Page<Patient> getDoctorPatients(Long userId, int page) {
        Long docId = getDoctorIdByUserId(userId);
        return patientRepo.findPatientsByDoctorId(getDoctorIdByUserId(userId), PageRequest.of(page, 15, Sort.by("id").descending()));
    }

    public List<Doctor> getAvailableDoctors(Long specializationId, Long branchId) {
        if (specializationId != null && branchId != null)
            return doctorRepo.findApprovedBySpecializationIdAndBranchId(
                specializationId, branchId);
        if (specializationId != null)
            return doctorRepo.findBySpecializationIdAndApprovalStatus(specializationId, "APPROVED");
        return doctorRepo.findByApprovalStatus("APPROVED");
    }

    public List<DoctorSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        return slotRepo.findAvailableSlots(doctorId, date);
    }
}
