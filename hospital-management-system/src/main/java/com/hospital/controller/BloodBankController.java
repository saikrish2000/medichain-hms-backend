package com.hospital.controller;

import com.hospital.enums.BloodGroup;
import com.hospital.service.BloodBankService;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/blood-bank")
@RequiredArgsConstructor
public class BloodBankController {

    private final BloodBankService bloodBankService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(bloodBankService.getDashboardStats());
    }

    @GetMapping("/inventory")
    public ResponseEntity<?> inventory() {
        return ResponseEntity.ok(bloodBankService.getAllInventory());
    }

    @PostMapping("/inventory")
    public ResponseEntity<?> updateInventory(@RequestBody Map<String,Object> body) {
        Long bankId = Long.parseLong(body.get("bankId").toString());
        BloodGroup group = BloodGroup.valueOf((String) body.get("bloodGroup"));
        int units = Integer.parseInt(body.get("units").toString());
        int threshold = Integer.parseInt(body.getOrDefault("minimumThreshold","5").toString());
        return ResponseEntity.ok(bloodBankService.setStock(bankId, group, units, threshold));
    }

    @PostMapping("/requests")
    public ResponseEntity<?> createRequest(@RequestBody Map<String,Object> body,
                                            @AuthenticationPrincipal UserPrincipal u) {
        Long bankId  = Long.parseLong(body.get("bankId").toString());
        BloodGroup g = BloodGroup.valueOf((String) body.get("bloodGroup"));
        int units    = Integer.parseInt(body.get("unitsRequested").toString());
        String urgency = (String) body.getOrDefault("urgencyLevel","NORMAL");
        String reason  = (String) body.getOrDefault("reason","");
        return ResponseEntity.ok(bloodBankService.createRequest(bankId, u.getId(), g, units, urgency, reason));
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                      @RequestBody Map<String,Object> body,
                                      @AuthenticationPrincipal UserPrincipal u) {
        int units = Integer.parseInt(body.get("unitsApproved").toString());
        bloodBankService.approveRequest(id, units, u.getId());
        return ResponseEntity.ok(Map.of("message","Request approved"));
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                     @RequestBody Map<String,String> body,
                                     @AuthenticationPrincipal UserPrincipal u) {
        bloodBankService.rejectRequest(id, body.get("reason"), u.getId());
        return ResponseEntity.ok(Map.of("message","Request rejected"));
    }

    @GetMapping("/donations")
    public ResponseEntity<?> donations() {
        return ResponseEntity.ok(bloodBankService.getAllDonations());
    }

    @PostMapping("/donations")
    public ResponseEntity<?> registerDonation(@RequestBody Map<String,Object> body,
                                               @AuthenticationPrincipal UserPrincipal u) {
        Long bankId  = Long.parseLong(body.get("bankId").toString());
        BloodGroup g = BloodGroup.valueOf((String) body.get("bloodGroup"));
        int units    = Integer.parseInt(body.get("unitsCollected").toString());
        return ResponseEntity.ok(bloodBankService.registerDonation(bankId, u.getId(), g, units));
    }

    @PostMapping("/donations/{id}/accept")
    public ResponseEntity<?> acceptDonation(@PathVariable Long id,
                                             @AuthenticationPrincipal UserPrincipal u) {
        bloodBankService.acceptDonation(id, u.getId());
        return ResponseEntity.ok(Map.of("message","Donation accepted and stock updated"));
    }
}
