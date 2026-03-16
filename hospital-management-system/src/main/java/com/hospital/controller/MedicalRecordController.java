package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.repository.PatientRepository;
import com.hospital.security.UserPrincipal;
import com.hospital.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctor/records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService recordService;
    private final PatientRepository    patientRepo;

    // Create record for a patient (from appointment)
    @GetMapping("/new")
    public String newRecord(@RequestParam Long patientId,
                             @RequestParam(required = false) Long appointmentId,
                             Model model) {
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        model.addAttribute("patient",       patient);
        model.addAttribute("appointmentId", appointmentId);
        model.addAttribute("form",          new MedicalRecord());
        return "doctor/record-form";
    }

    @PostMapping("/save")
    public String saveRecord(@AuthenticationPrincipal UserPrincipal user,
                              @RequestParam Long patientId,
                              @RequestParam(required = false) Long appointmentId,
                              @ModelAttribute("form") MedicalRecord form,
                              RedirectAttributes ra) {
        recordService.createRecord(user.getId(), patientId, appointmentId, form);
        ra.addFlashAttribute("success", "Medical record saved successfully.");
        return "redirect:/doctor/patients";
    }

    @GetMapping("/{id}/edit")
    public String editRecord(@PathVariable Long id, Model model) {
        MedicalRecord record = recordService.getRecord(id);
        model.addAttribute("record",  record);
        model.addAttribute("patient", record.getPatient());
        return "doctor/record-edit";
    }

    @PostMapping("/{id}/update")
    public String updateRecord(@PathVariable Long id,
                                @ModelAttribute MedicalRecord form,
                                RedirectAttributes ra) {
        recordService.updateRecord(id, form);
        ra.addFlashAttribute("success", "Record updated.");
        return "redirect:/doctor/patients";
    }
}
