package com.hospital.controller;

import com.hospital.service.GpsTrackingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequiredArgsConstructor
@Tag(name = "GPS Tracking", description = "Real-time ambulance GPS")
public class GpsController {

    private final GpsTrackingService gpsService;

    @PostMapping("/api/ambulance/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        gpsService.updateLocation(id,
            Double.parseDouble(body.get("latitude").toString()),
            Double.parseDouble(body.get("longitude").toString()),
            body.containsKey("speed") ? Double.parseDouble(body.get("speed").toString()) : 0,
            body.getOrDefault("heading","N").toString());
        return ResponseEntity.ok(Map.of("status","updated","ambulanceId",id));
    }

    @GetMapping("/api/ambulance/{id}/location")
    public ResponseEntity<?> getLocation(@PathVariable Long id) {
        return ResponseEntity.ok(gpsService.getLastLocation(id));
    }

    @GetMapping("/api/ambulance/locations/all")
    public ResponseEntity<?> allLocations() {
        return ResponseEntity.ok(gpsService.getAllLocations());
    }

    @MessageMapping("/ambulance/location")
    public void wsLocation(@Payload Map<String,Object> p) {
        gpsService.updateLocation(Long.parseLong(p.get("ambulanceId").toString()),
            Double.parseDouble(p.get("latitude").toString()),
            Double.parseDouble(p.get("longitude").toString()),
            p.containsKey("speed") ? Double.parseDouble(p.get("speed").toString()) : 0,
            p.getOrDefault("heading","N").toString());
    }
}
