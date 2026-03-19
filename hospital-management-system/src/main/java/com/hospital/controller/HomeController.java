package com.hospital.controller;

import com.hospital.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserPrincipal user) {
        // If already logged in, redirect to role dashboard
        if (user != null) {
            return switch (user.getRole().name()) {
                case "ADMIN"               -> "redirect:/admin/dashboard";
                case "DOCTOR"              -> "redirect:/doctor/dashboard";
                case "NURSE",
                     "INDEPENDENT_NURSE"   -> "redirect:/nurse/dashboard";
                case "PATIENT"             -> "redirect:/patient/dashboard";
                case "BLOOD_BANK_MANAGER"  -> "redirect:/blood-bank/dashboard";
                case "AMBULANCE_OPERATOR"  -> "redirect:/ambulance/dashboard";
                case "PHARMACIST"          -> "redirect:/pharmacy/dashboard";
                case "LAB_TECHNICIAN",
                     "PHLEBOTOMIST"        -> "redirect:/lab/dashboard";
                case "MEDICAL_SHOP_OWNER"  -> "redirect:/medical-shop/dashboard";
                case "DIAGNOSTIC_CENTER_OWNER" -> "redirect:/diagnostic/dashboard";
                default                    -> "redirect:/patient/dashboard";
            };
        }
        return "home";
    }
}
