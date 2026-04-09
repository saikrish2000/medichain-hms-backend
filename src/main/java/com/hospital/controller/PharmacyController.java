package com.hospital.controller;

import com.hospital.entity.Medicine;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.service.PharmacyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hospital.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;

    @GetMapping("/medicines")
    public ResponseEntity<?> medicines(@RequestParam(required=false) String q,
                                        @RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(pharmacyService.searchMedicines(q, page));
    }

    @GetMapping("/medicines/low-stock")
    public ResponseEntity<?> lowStock() {
        return ResponseEntity.ok(pharmacyService.getLowStockMedicines());
    }

    @PostMapping("/medicines")
    public ResponseEntity<?> saveMedicine(@RequestBody Medicine medicine) {
        return ResponseEntity.ok(pharmacyService.saveMedicine(medicine));
    }

    @PatchMapping("/medicines/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id,
                                          @RequestBody Map<String,Object> body) {
        int qty = Integer.parseInt(body.get("quantity").toString());
        String op = (String) body.getOrDefault("operation","add");
        return ResponseEntity.ok(pharmacyService.updateStock(id, qty, op));
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<?> prescriptions(@RequestParam(defaultValue="0") int page) {
        return ResponseEntity.ok(pharmacyService.getPendingPrescriptions(page));
    }

    @PostMapping("/prescriptions/{id}/dispense")
    public ResponseEntity<?> dispense(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal u) {
        return ResponseEntity.ok(pharmacyService.dispensePrescription(id, u));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(pharmacyService.getDashboardStats());
    }
}
