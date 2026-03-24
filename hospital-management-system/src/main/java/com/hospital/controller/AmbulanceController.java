package com.hospital.controller;

import com.hospital.entity.Ambulance;
import com.hospital.entity.AmbulanceCall;
import com.hospital.entity.AmbulanceCall.CallStatus;
import com.hospital.security.UserPrincipal;
import com.hospital.service.AmbulanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ambulance")
@RequiredArgsConstructor
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(ambulanceService.getDashboardStats());
        return "ambulance/dashboard";
    }

    // ── DISPATCH ───────────────────────────────────────────
    @GetMapping("/dispatch")
    public String dispatchForm(Model model) {
        model.addAttribute("available", ambulanceService.getAvailableAmbulances());
        return "ambulance/dispatch";
    }

    @PostMapping("/dispatch")
    public String dispatch(@RequestParam String callerName,
                           @RequestParam String callerPhone,
                           @RequestParam String pickupAddress,
                           @RequestParam(required = false) Double lat,
                           @RequestParam(required = false) Double lng,
                           @RequestParam(required = false) String emergencyType,
                           RedirectAttributes ra) {
        try {
            AmbulanceCall call = ambulanceService.requestAmbulance(
                callerName, callerPhone, pickupAddress, lat, lng, emergencyType, null);
            ra.addFlashAttribute("success",
                "Ambulance dispatched! Call ID: #" + call.getId());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ambulance/calls";
    }

    // ── CALLS ──────────────────────────────────────────────
    @GetMapping("/calls")
    public String calls(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("calls", ambulanceService.getAllCalls(page));
        return "ambulance/calls";
    }

    @PostMapping("/calls/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        try {
            ambulanceService.updateCallStatus(id, CallStatus.valueOf(status));
            ra.addFlashAttribute("success", "Status updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ambulance/calls";
    }

    // ── FLEET MANAGEMENT ───────────────────────────────────
    @GetMapping("/fleet")
    public String fleet(Model model) {
        model.addAttribute("ambulances", ambulanceService.getAllAmbulances());
        return "ambulance/fleet";
    }

    @GetMapping("/fleet/new")
    public String newAmbulanceForm(Model model) {
        model.addAttribute("ambulance", new Ambulance());
        model.addAttribute("statuses", Ambulance.AmbulanceStatus.values());
        return "ambulance/ambulance-form";
    }

    @PostMapping("/fleet/save")
    public String saveAmbulance(@ModelAttribute Ambulance ambulance, RedirectAttributes ra) {
        try {
            ambulanceService.saveAmbulance(ambulance);
            ra.addFlashAttribute("success", "Ambulance saved!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ambulance/fleet";
    }

    // ── GPS LOCATION UPDATE (called by operator app/JS) ────
    @PostMapping("/location/{id}")
    @ResponseBody
    public String updateLocation(@PathVariable Long id,
                                  @RequestParam Double lat,
                                  @RequestParam Double lng) {
        ambulanceService.updateLocation(id, lat, lng);
        return "OK";
    }

    // ── TRACKING PAGE (public) ─────────────────────────────
    @GetMapping("/track/{callId}")
    public String trackCall(@PathVariable Long callId, Model model) {
        model.addAttribute("callId", callId);
        return "ambulance/track";
    }
}
