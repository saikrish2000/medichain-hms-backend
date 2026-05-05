package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository           userRepo;
    private final DoctorRepository         doctorRepo;
    private final PatientRepository        patientRepo;
    private final NurseRepository          nurseRepo;
    private final DepartmentRepository     deptRepo;
    private final SpecializationRepository specRepo;
    private final HospitalBranchRepository branchRepo;
    private final AppointmentRepository    apptRepo;
    private final AuditLogRepository       auditRepo;
    private final InvoiceRepository        invoiceRepo;

    // ── Dashboard ──────────────────────────────────────────────────────────
    public Map<String,Object> getDashboardStats() {
        Map<String,Object> stats = new LinkedHashMap<>();
        stats.put("totalDoctors",        doctorRepo.count());
        stats.put("totalPatients",       patientRepo.count());
        stats.put("totalNurses",         nurseRepo.count());
        stats.put("pendingDoctors",      doctorRepo.countByApprovalStatus("PENDING"));
        stats.put("todayAppointments",   apptRepo.countByAppointmentDate(LocalDate.now()));
        stats.put("activeCalls",         0L);
        stats.put("pendingBloodRequests",0L);
        stats.put("todayRevenue",        0L);
        var recentPts = patientRepo.findAll(PageRequest.of(0,5,Sort.by(Sort.Direction.DESC,"createdAt")));
        stats.put("recentPatients",      recentPts.getContent());
        return stats;
    }

    // ── Doctors ───────────────────────────────────────────────────────────
    public Page<Doctor> getAllDoctors(int page) {
        return doctorRepo.findAll(PageRequest.of(page,20,Sort.by("createdAt").descending()));
    }

    @Transactional
    public void approveDoctor(Long doctorId) {
        Doctor d = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor","id",doctorId));
        d.setApprovalStatus("APPROVED");
        d.getUser().setApprovalStatus("APPROVED");
        d.getUser().setEnabled(true);
        doctorRepo.save(d);
    }

    @Transactional
    public void rejectDoctor(Long doctorId) {
        Doctor d = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor","id",doctorId));
        d.setApprovalStatus("REJECTED");
        d.getUser().setApprovalStatus("REJECTED");
        doctorRepo.save(d);
    }

    // ── Nurses ────────────────────────────────────────────────────────────
    public Page<Nurse> getAllNurses(int page) {
        return nurseRepo.findAll(PageRequest.of(page,20,Sort.by("id").descending()));
    }

    @Transactional
    public void approveNurse(Long nurseId) {
        Nurse n = nurseRepo.findById(nurseId)
            .orElseThrow(() -> new ResourceNotFoundException("Nurse","id",nurseId));
        n.getUser().setApprovalStatus("APPROVED");
        n.getUser().setEnabled(true);
        nurseRepo.save(n);
    }

    // ── Patients ──────────────────────────────────────────────────────────
    public Page<Patient> getAllPatients(int page) {
        return patientRepo.findAll(PageRequest.of(page,20,Sort.by("createdAt").descending()));
    }

    public Page<Patient> searchPatients(String q, int page) {
        return patientRepo.searchByKeyword(q, PageRequest.of(page,20));
    }

    // ── Departments ───────────────────────────────────────────────────────
    public List<Department> getAllDepartments() { return deptRepo.findAll(); }

    @Transactional
    public Department createDepartment(Department dept) {
        if (dept.getBranch() == null)
            branchRepo.findById(1L).ifPresent(dept::setBranch);
        return deptRepo.save(dept);
    }

    public void deleteDepartment(Long id) { deptRepo.deleteById(id); }

    // ── Specializations ───────────────────────────────────────────────────
    public List<Specialization> getAllSpecializations() { return specRepo.findAll(); }

    // ── Branches ──────────────────────────────────────────────────────────
    public List<HospitalBranch> getAllBranches() { return branchRepo.findAll(); }

    // ── Users ─────────────────────────────────────────────────────────────
    public Page<User> getAllUsers(int page) {
        return userRepo.findAll(PageRequest.of(page,20,Sort.by("createdAt").descending()));
    }

    // ── Audit Logs ────────────────────────────────────────────────────────
    public Page<AuditLog> getAuditLogs(int page) {
        return auditRepo.findAll(PageRequest.of(page,20,Sort.by(Sort.Direction.DESC,"createdAt")));
    }

    // ── Reports ───────────────────────────────────────────────────────────
    public Map<String,Object> getReports(String from, String to) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fromDate = LocalDate.parse(from, fmt);
        LocalDate toDate   = LocalDate.parse(to,   fmt);
        Map<String,Object> report = new LinkedHashMap<>();
        report.put("totalAppointments", apptRepo.countByAppointmentDateBetween(fromDate, toDate));
        report.put("completedAppts",    apptRepo.countByStatusAndAppointmentDateBetween("COMPLETED", fromDate, toDate));
        report.put("cancelledAppts",    apptRepo.countByStatusAndAppointmentDateBetween("CANCELLED", fromDate, toDate));
        report.put("newPatients",       patientRepo.countByCreatedAtBetween(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay()));
        report.put("activeDoctors",     doctorRepo.countByApprovalStatus("APPROVED"));
        report.put("labOrders",         0L);
        report.put("prescriptions",     0L);
        report.put("totalRevenue",      0L);
        return report;
    }
}
