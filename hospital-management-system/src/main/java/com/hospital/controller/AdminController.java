package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import com.hospital.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService             adminService;
    private final DepartmentRepository     departmentRepository;
    private final SpecializationRepository specializationRepository;
    private final HospitalBranchRepository branchRepository;
    private final DoctorRepository         doctorRepository;
    private final NurseRepository          nurseRepository;
    private final PatientRepository        patientRepository;

    // ── DASHBOARD ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @AuthenticationPrincipal UserPrincipal admin) {
        Map<String, Object> stats = adminService.getDashboardStats();
        model.addAllAttributes(stats);
        model.addAttribute("adminName", admin.getFullName());
        return "admin/dashboard";
    }

    // ── DOCTOR APPROVALS ───────────────────────────────────
    @GetMapping("/approvals/doctors")
    public String pendingDoctors(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "") String filter,
                                  Model model) {
        if ("all".equals(filter)) {
            model.addAttribute("doctors",
                adminService.getAllDoctors(null, page));
            model.addAttribute("filter", "all");
        } else {
            model.addAttribute("doctors",
                adminService.getPendingDoctors(page));
            model.addAttribute("filter", "pending");
        }
        return "admin/approvals/doctors";
    }

    @PostMapping("/approvals/doctors/{id}/approve")
    public String approveDoctor(@PathVariable Long id,
                                @AuthenticationPrincipal UserPrincipal admin,
                                RedirectAttributes ra) {
        try {
            adminService.approveDoctor(id, admin);
            ra.addFlashAttribute("success", "Doctor approved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/approvals/doctors";
    }

    @PostMapping("/approvals/doctors/{id}/reject")
    public String rejectDoctor(@PathVariable Long id,
                               @RequestParam String reason,
                               @AuthenticationPrincipal UserPrincipal admin,
                               RedirectAttributes ra) {
        try {
            adminService.rejectDoctor(id, reason, admin);
            ra.addFlashAttribute("success", "Doctor application rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/approvals/doctors";
    }

    @PostMapping("/approvals/doctors/{id}/suspend")
    public String suspendDoctor(@PathVariable Long id,
                                @RequestParam String reason,
                                @AuthenticationPrincipal UserPrincipal admin,
                                RedirectAttributes ra) {
        adminService.suspendDoctor(id, reason, admin);
        ra.addFlashAttribute("success", "Doctor account suspended.");
        return "redirect:/admin/approvals/doctors?filter=all";
    }

    @GetMapping("/approvals/doctors/{id}")
    public String doctorDetail(@PathVariable Long id, Model model) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Doctor not found"));
        model.addAttribute("doctor", doctor);
        return "admin/approvals/doctor-detail";
    }

    // ── NURSE APPROVALS ────────────────────────────────────
    @GetMapping("/approvals/nurses")
    public String pendingNurses(@RequestParam(defaultValue = "0") int page,
                                 Model model) {
        model.addAttribute("nurses", adminService.getPendingNurses(page));
        return "admin/approvals/nurses";
    }

    @PostMapping("/approvals/nurses/{id}/approve")
    public String approveNurse(@PathVariable Long id,
                               @AuthenticationPrincipal UserPrincipal admin,
                               RedirectAttributes ra) {
        try {
            adminService.approveNurse(id, admin);
            ra.addFlashAttribute("success", "Nurse approved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/approvals/nurses";
    }

    @PostMapping("/approvals/nurses/{id}/reject")
    public String rejectNurse(@PathVariable Long id,
                              @RequestParam String reason,
                              @AuthenticationPrincipal UserPrincipal admin,
                              RedirectAttributes ra) {
        adminService.rejectNurse(id, reason, admin);
        ra.addFlashAttribute("success", "Nurse application rejected.");
        return "redirect:/admin/approvals/nurses";
    }

    // ── DEPARTMENTS ────────────────────────────────────────
    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", adminService.getAllDepartments());
        model.addAttribute("branches",    adminService.getAllBranches());
        model.addAttribute("newDept",     new Department());
        return "admin/departments";
    }

    @PostMapping("/departments/create")
    public String createDepartment(@ModelAttribute Department dept,
                                   @RequestParam Long branchId,
                                   RedirectAttributes ra) {
        try {
            HospitalBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BadRequestException("Branch not found"));
            dept.setBranch(branch);
            adminService.createDepartment(dept);
            ra.addFlashAttribute("success", "Department created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @PostMapping("/departments/{id}/toggle")
    public String toggleDepartment(@PathVariable Long id, RedirectAttributes ra) {
        Department dept = departmentRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Department not found"));
        dept.setIsActive(!dept.getIsActive());
        departmentRepository.save(dept);
        ra.addFlashAttribute("success", "Department status updated.");
        return "redirect:/admin/departments";
    }

    // ── SPECIALIZATIONS ────────────────────────────────────
    @GetMapping("/specializations")
    public String specializations(Model model) {
        model.addAttribute("specs",        adminService.getAllSpecializations());
        model.addAttribute("departments",  adminService.getAllDepartments());
        model.addAttribute("newSpec",      new Specialization());
        return "admin/specializations";
    }

    @PostMapping("/specializations/create")
    public String createSpec(@ModelAttribute Specialization spec,
                             @RequestParam(required = false) Long departmentId,
                             RedirectAttributes ra) {
        try {
            if (departmentId != null)
                departmentRepository.findById(departmentId)
                    .ifPresent(spec::setDepartment);
            adminService.createSpecialization(spec);
            ra.addFlashAttribute("success", "Specialization created!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/specializations";
    }

    // ── BRANCHES ──────────────────────────────────────────
    @GetMapping("/branches")
    public String branches(Model model) {
        model.addAttribute("branches", adminService.getAllBranches());
        model.addAttribute("newBranch", new HospitalBranch());
        return "admin/branches";
    }

    @PostMapping("/branches/create")
    public String createBranch(@ModelAttribute HospitalBranch branch,
                               RedirectAttributes ra) {
        try {
            adminService.createBranch(branch);
            ra.addFlashAttribute("success", "Hospital branch created!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/branches";
    }

    // ── USERS ─────────────────────────────────────────────
    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("users", adminService.getAllUsers(page));
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id,
                             @AuthenticationPrincipal UserPrincipal admin,
                             RedirectAttributes ra) {
        adminService.toggleUserStatus(id, admin);
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }
}
