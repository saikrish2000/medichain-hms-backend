package com.hospital.controller;

import com.hospital.entity.*;
import com.hospital.entity.BloodRequest.RequesterType;
import com.hospital.enums.BloodGroup;
import com.hospital.repository.BloodBankRepository;
import com.hospital.security.UserPrincipal;
import com.hospital.service.BloodBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/blood-bank")
@RequiredArgsConstructor
public class BloodBankController {

    private final BloodBankService    bloodBankService;
    private final BloodBankRepository bankRepo;

    // ── DASHBOARD ──────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal user, Model model) {
        // For now, show all banks; in multi-branch setup, filter by user's branch
        List<BloodBank> banks = bankRepo.findByIsActive(true);
        if (banks.isEmpty()) {
            model.addAttribute("noBanks", true);
            return "blood-bank/dashboard";
        }
        BloodBank bank = banks.get(0);   // primary bank for this manager
        model.addAttribute("bank",      bank);
        model.addAttribute("inventory", bloodBankService.getInventory(bank.getId()));
        model.addAttribute("stats",     bloodBankService.getDashboardStats(bank.getId()));
        model.addAttribute("lowStock",  bloodBankService.getLowStockAlerts());
        model.addAttribute("pending",   bloodBankService.getPendingRequests(bank.getId(), 0));
        return "blood-bank/dashboard";
    }

    // ── INVENTORY ──────────────────────────────────────────
    @GetMapping("/inventory")
    public String inventory(@AuthenticationPrincipal UserPrincipal user, Model model) {
        List<BloodBank> banks = bankRepo.findByIsActive(true);
        BloodBank bank = banks.get(0);
        model.addAttribute("bank",        bank);
        model.addAttribute("inventory",   bloodBankService.getInventory(bank.getId()));
        model.addAttribute("bloodGroups", BloodGroup.values());
        return "blood-bank/inventory";
    }

    @PostMapping("/inventory/update")
    public String updateInventory(@RequestParam Long bankId,
                                   @RequestParam String bloodGroup,
                                   @RequestParam int units,
                                   @RequestParam(defaultValue = "5") int threshold,
                                   RedirectAttributes ra) {
        bloodBankService.setStock(bankId, BloodGroup.valueOf(bloodGroup), units, threshold);
        ra.addFlashAttribute("success", "Inventory updated for " + bloodGroup);
        return "redirect:/blood-bank/inventory";
    }

    @PostMapping("/inventory/adjust")
    public String adjustInventory(@RequestParam Long bankId,
                                   @RequestParam String bloodGroup,
                                   @RequestParam int delta,
                                   RedirectAttributes ra) {
        bloodBankService.updateStock(bankId, BloodGroup.valueOf(bloodGroup), delta);
        ra.addFlashAttribute("success", "Stock adjusted.");
        return "redirect:/blood-bank/inventory";
    }

    // ── REQUESTS ───────────────────────────────────────────
    @GetMapping("/requests")
    public String requests(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "pending") String filter,
                           Model model) {
        List<BloodBank> banks = bankRepo.findByIsActive(true);
        BloodBank bank = banks.get(0);
        if ("pending".equals(filter))
            model.addAttribute("requests", bloodBankService.getPendingRequests(bank.getId(), page));
        else
            model.addAttribute("requests", bloodBankService.getAllRequests(bank.getId(), page));
        model.addAttribute("filter", filter);
        model.addAttribute("bank",   bank);
        return "blood-bank/requests";
    }

    @PostMapping("/requests/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                  @RequestParam int unitsApproved,
                                  @AuthenticationPrincipal UserPrincipal user,
                                  RedirectAttributes ra) {
        try {
            bloodBankService.approveRequest(id, unitsApproved, user.getId());
            ra.addFlashAttribute("success", "Request approved.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/blood-bank/requests";
    }

    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                 @RequestParam String reason,
                                 @AuthenticationPrincipal UserPrincipal user,
                                 RedirectAttributes ra) {
        bloodBankService.rejectRequest(id, reason, user.getId());
        ra.addFlashAttribute("success", "Request rejected.");
        return "redirect:/blood-bank/requests";
    }

    // ── DONATIONS ──────────────────────────────────────────
    @GetMapping("/donations")
    public String donations(@RequestParam(defaultValue = "0") int page, Model model) {
        List<BloodBank> banks = bankRepo.findByIsActive(true);
        BloodBank bank = banks.get(0);
        model.addAttribute("bank",        bank);
        model.addAttribute("bloodGroups", BloodGroup.values());
        return "blood-bank/donations";
    }

    @PostMapping("/donations/{id}/accept")
    public String acceptDonation(@PathVariable Long id,
                                  @AuthenticationPrincipal UserPrincipal user,
                                  RedirectAttributes ra) {
        bloodBankService.acceptDonation(id, user.getId());
        ra.addFlashAttribute("success", "Donation accepted and added to inventory.");
        return "redirect:/blood-bank/donations";
    }
}

// ── PUBLIC BLOOD REQUEST (any logged-in user) ──────────────
// Separate controller at /blood-request
