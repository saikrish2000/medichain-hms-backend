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
public class AdminService {

    private final DoctorRepository         doctorRepo;
    private final NurseRepository          nurseRepo;
    private final PatientRepository        patientRepo;
    private final UserRepository           userRepo;
    private final DepartmentRepository     departmentRepo;
    private final SpecializationRepository specializationRepo;
    private final HospitalBranchRepository branchRepo;
    private final AppointmentRepository    appointmentRepo;
    private final NotificationService      notificationService;
    private final AuditService             auditService;

    // ── DASHBOARD ─────────────────────────────────────────

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalDoctors  = doctorRepo.countByApprovalStatus(ApprovalStatus.APPROVED);
        long pendingDoctors= doctorRepo.countByApprovalStatus(ApprovalStatus.PENDING);
        long pendingNurses = nurseRepo.countByApprovalStatus(Nurse.ApprovalStatus.PENDING);
        long totalPatients = patientRepo.count();
        long totalBranches = branchRepo.count();

        stats.put("totalDoctors",     totalDoctors);
        stats.put("pendingDoctors",   pendingDoctors);
        stats.put("pendingNurses",    pendingNurses);
        stats.put("totalPatients",    totalPatients);
        stats.put("totalBranches",    totalBranches);
        stats.put("totalDepartments", departmentRepo.countByIsActive(true));
        stats.put("todayAppointments", 0L); // Placeholder — billing module calculates this
        stats.put("todayRevenue",      "0");

        // Pending approvals list (first 5 doctors)
        stats.put("pendingApprovals",
            doctorRepo.findByApprovalStatus(ApprovalStatus.PENDING,
                PageRequest.of(0, 5)).getContent());

        // Recent patients
        stats.put("recentPatients",
            patientRepo.findAll(PageRequest.of(0, 5,
                Sort.by("id").descending())).getContent());

        return stats;
    }

    // ── DOCTOR APPROVALS ──────────────────────────────────

    public Page<Doctor> getPendingDoctors(int page) {
        return doctorRepo.findByApprovalStatus(ApprovalStatus.PENDING,
            PageRequest.of(page, 15, Sort.by("id").descending()));
    }

    public Page<Doctor> getAllDoctors(ApprovalStatus status, int page) {
        if (status != null)
            return doctorRepo.findByApprovalStatus(status, PageRequest.of(page, 20));
        return doctorRepo.findAll(PageRequest.of(page, 20, Sort.by("id").descending()));
    }

    @Transactional
    public void approveDoctor(Long doctorId, UserPrincipal admin) {
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        doctor.setApprovalStatus(ApprovalStatus.APPROVED);
        doctor.setIsAvailable(true);
        doctorRepo.save(doctor);
        auditService.log(admin.getUsername(), "APPROVE",
            "Doctor", doctorId, "Doctor approved: " + doctor.getUser().getFullName());
        notificationService.sendApprovalNotification(
            doctor.getUser().getEmail(),
            doctor.getUser().getFullName(),
            "Doctor");
    }

    @Transactional
    public void rejectDoctor(Long doctorId, String reason, UserPrincipal admin) {
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        doctor.setApprovalStatus(ApprovalStatus.REJECTED);
        doctorRepo.save(doctor);
        auditService.log(admin.getUsername(), "REJECT",
            "Doctor", doctorId, "Rejected: " + reason);
        notificationService.sendRejectionNotification(
            doctor.getUser().getEmail(),
            doctor.getUser().getFullName(),
            reason);
    }

    @Transactional
    public void suspendDoctor(Long doctorId, String reason, UserPrincipal admin) {
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
        doctor.setApprovalStatus(ApprovalStatus.SUSPENDED);
        doctor.setIsAvailable(false);
        doctorRepo.save(doctor);
        auditService.log(admin.getUsername(), "SUSPEND",
            "Doctor", doctorId, "Suspended: " + reason);
    }

    // ── NURSE APPROVALS ───────────────────────────────────

    public Page<Nurse> getPendingNurses(int page) {
        return nurseRepo.findByApprovalStatus(Nurse.ApprovalStatus.PENDING,
            PageRequest.of(page, 15, Sort.by("id").descending()));
    }

    @Transactional
    public void approveNurse(Long nurseId, UserPrincipal admin) {
        Nurse nurse = nurseRepo.findById(nurseId)
            .orElseThrow(() -> new ResourceNotFoundException("Nurse", "id", nurseId));
        nurse.setApprovalStatus(Nurse.ApprovalStatus.APPROVED);
        nurseRepo.save(nurse);
        auditService.log(admin.getUsername(), "APPROVE",
            "Nurse", nurseId, "Nurse approved: " + nurse.getUser().getFullName());
        notificationService.sendApprovalNotification(
            nurse.getUser().getEmail(),
            nurse.getUser().getFullName(),
            "Nurse");
    }

    @Transactional
    public void rejectNurse(Long nurseId, String reason, UserPrincipal admin) {
        Nurse nurse = nurseRepo.findById(nurseId)
            .orElseThrow(() -> new ResourceNotFoundException("Nurse", "id", nurseId));
        nurse.setApprovalStatus(Nurse.ApprovalStatus.REJECTED);
        nurseRepo.save(nurse);
        auditService.log(admin.getUsername(), "REJECT",
            "Nurse", nurseId, "Rejected: " + reason);
    }

    // ── DEPARTMENTS ───────────────────────────────────────

    public List<Department> getAllDepartments() {
        return departmentRepo.findAll(Sort.by("name"));
    }

    @Transactional
    public Department createDepartment(Department dept) {
        return departmentRepo.save(dept);
    }

    // ── SPECIALIZATIONS ───────────────────────────────────

    public List<Specialization> getAllSpecializations() {
        return specializationRepo.findAll(Sort.by("name"));
    }

    @Transactional
    public Specialization createSpecialization(Specialization spec) {
        return specializationRepo.save(spec);
    }

    // ── BRANCHES ──────────────────────────────────────────

    public List<HospitalBranch> getAllBranches() {
        return branchRepo.findAll(Sort.by("name"));
    }

    @Transactional
    public HospitalBranch createBranch(HospitalBranch branch) {
        return branchRepo.save(branch);
    }

    // ── USERS ─────────────────────────────────────────────

    public Page<User> getAllUsers(int page) {
        return userRepo.findAll(PageRequest.of(page, 20, Sort.by("id").descending()));
    }

    @Transactional
    public void toggleUserStatus(Long userId, UserPrincipal admin) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(!user.getIsActive());
        userRepo.save(user);
        auditService.log(admin.getUsername(), "UPDATE",
            "User", userId,
            "User " + (user.getIsActive() ? "activated" : "deactivated"));
    }

    // ── PATIENTS ──────────────────────────────────────────

    public Page<Patient> getAllPatients(int page) {
        return patientRepo.findAll(PageRequest.of(page, 20, Sort.by("id").descending()));
    }

    public Page<Patient> searchPatients(String q, int page) {
        return patientRepo.search(q, PageRequest.of(page, 20));
    }
}
