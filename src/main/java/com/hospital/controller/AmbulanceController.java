package com.hospital.controller;

import com.hospital.entity.Ambulance;
import com.hospital.service.AmbulanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ambulance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AMBULANCE_OPERATOR','ADMIN')")
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(ambulanceService.getDashboardStats());
    }

    @GetMapping("/fleet")
    public ResponseEntity<?> fleet() {
        return ResponseEntity.ok(ambulanceService.getAllAmbulances());
    }

    @PostMapping("/fleet")
    public ResponseEntity<?> addAmbulance(@RequestBody Ambulance ambulance) {
        return ResponseEntity.ok(ambulanceService.saveAmbulance(ambulance));
    }

    @GetMapping("/calls")
    public ResponseEntity<?> calls(@RequestParam(defaultValue="0") int page,
                                   @RequestParam(required=false) String status) {
        return ResponseEntity.ok(ambulanceService.getAllCalls(page));
    }

    @PostMapping("/dispatch")
    public ResponseEntity<?> dispatch(@RequestBody Map<String,Object> body) {
        String caller = (String) body.get("callerName");
        String phone  = (String) body.get("callerPhone");
        String addr   = (String) body.get("pickupAddress");
        String type   = (String) body.getOrDefault("emergencyType","GENERAL");
        return ResponseEntity.ok(ambulanceService.requestAmbulance(caller, phone, addr, type));
    }

    @PatchMapping("/calls/{id}/status")
    public ResponseEntity<?> updateCallStatus(@PathVariable Long id,
                                              @RequestBody Map<String,String> body) {
        return ResponseEntity.ok(ambulanceService.updateCallStatus(id, body.get("status").toUpperCase()));
    }

    @PatchMapping("/fleet/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable Long id,
                                            @RequestBody Map<String,Object> body) {
        double lat = Double.parseDouble(body.get("latitude").toString());
        double lng = Double.parseDouble(body.get("longitude").toString());
        ambulanceService.updateLocation(id, lat, lng);
        return ResponseEntity.ok(Map.of("message","Location updated"));
    }
}
