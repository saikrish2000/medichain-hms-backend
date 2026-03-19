package com.hospital.controller;

import com.hospital.entity.Doctor;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/nurse")
@RequiredArgsConstructor
public class NurseController {

    private final NurseRepository nurseRepo;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal user, Model model) {
        // In full impl: load assigned patients, tasks, alerts
        // For now: render with mock data baked into template
        return "nurse/dashboard";
    }

    @GetMapping("/patients")
    public String patients(Model model) {
        return "nurse/patients";
    }

    @GetMapping("/tasks")
    public String tasks(Model model) {
        return "nurse/tasks";
    }

    @GetMapping("/emar")
    public String emar(Model model) {
        return "nurse/emar";
    }

    @GetMapping("/vitals")
    public String vitals(Model model) {
        return "nurse/vitals";
    }

    @GetMapping("/ews")
    public String ews(Model model) {
        return "nurse/ews";
    }

    @GetMapping("/handover")
    public String handover(Model model) {
        return "nurse/handover";
    }
}
