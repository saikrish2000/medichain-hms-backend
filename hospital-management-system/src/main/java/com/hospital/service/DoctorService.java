package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.Doctor.ApprovalStatus;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
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
public class DoctorService {

    private final DoctorRepository        doctorRepo;
    private final UserRepository           userRepo;
    private final AppointmentRepository    apptRepo;
    private final MedicalRecordRepository  recordRepo;
    private final SlotService              slotService;

    // ── PROFILE ───────────────────────────────────────────
    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", userId));
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
    }

    @Transactional
    public Doctor updateProfile(Long userId, Doctor updated) {
        Doctor doctor = getDoctorByUserId(userId);
        doctor.setBio(updated.getBio());
        doctor.setConsultationFee(updated.getConsultationFee());
        doctor.setExperienceYears(updated.getExperienceYears());
        doctor.setQualification(updated.getQualification());
        doctor.setIsAvailable(updated.getIsAvailable());
        return doctorRepo.save(doctor);
    }

    // ── DASHBOARD STATS ───────────────────────────────────
    public Map<String, Object> getDoctorDashboard(Long doctorId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("todayAppointments",
            apptRepo.findByDoctorIdAndAppointmentDateOrderByAppointmentTime(doctorId, LocalDate.now()));
        data.put("pendingCount",
            apptRepo.countByDoctorIdAndStatus(doctorId, Appointment.AppointmentStatus.PENDING));
        data.put("confirmedCount",
            apptRepo.countByDoctorIdAndStatus(doctorId, Appointment.AppointmentStatus.CONFIRMED));
        data.put("completedCount",
            apptRepo.countByDoctorIdAndStatus(doctorId, Appointment.AppointmentStatus.COMPLETED));
        data.put("todayCount",
            apptRepo.countByDoctorIdAndAppointmentDate(doctorId, LocalDate.now()));
        data.put("weekCalendar",
            slotService.getWeekCalendar(doctorId, LocalDate.now().with(
                java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))));
        return data;
    }

    // ── SEARCH ────────────────────────────────────────────
    public Page<Doctor> searchDoctors(String query, Long departmentId, Long specializationId, int page) {
        if (query != null && !query.isBlank())
            return doctorRepo.search(query, PageRequest.of(page, 12, Sort.by("createdAt").descending()));
        return doctorRepo.findByApprovalStatus(
            ApprovalStatus.APPROVED, PageRequest.of(page, 12, Sort.by("createdAt").descending()));
    }

    // ── AVAILABLE DOCTORS ─────────────────────────────────
    public List<Doctor> getAvailableDoctorsByDepartment(Long departmentId) {
        return doctorRepo.findByDepartmentId(departmentId)
            .stream()
            .filter(d -> d.getApprovalStatus() == ApprovalStatus.APPROVED && Boolean.TRUE.equals(d.getIsAvailable()))
            .toList();
    }
}
