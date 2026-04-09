package com.hospital.controller;

import com.hospital.service.GpsTrackingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "GPS Tracking", description = "Real-time ambulance GPS tracking via WebSocket")
public class GpsController {

    private final GpsTrackingService gpsTrackingService;

    /**
     * REST: Update ambulance location (used by ambulance app/device)
     * POST /api/ambulance/{id}/location
     */
    @PostMapping("/api/ambulance/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body) {
        double lat     = Double.parseDouble(body.get("latitude").toString());
        double lng     = Double.parseDouble(body.get("longitude").toString());
        double speed   = body.containsKey("speed") ? Double.parseDouble(body.get("speed").toString()) : 0;
        String heading = body.getOrDefault("heading", "N").toString();
        gpsTrackingService.updateLocation(id, lat, lng, speed, heading);
        return ResponseEntity.ok(Map.of("status", "location_updated", "ambulanceId", id));
    }

    /** REST: Get last known location */
    @GetMapping("/api/ambulance/{id}/location")
    public ResponseEntity<?> getLocation(@PathVariable Long id) {
        return ResponseEntity.ok(gpsTrackingService.getLastLocation(id));
    }

    /** REST: Get all active ambulance locations */
    @GetMapping("/api/ambulance/locations/all")
    public ResponseEntity<?> getAllLocations() {
        return ResponseEntity.ok(gpsTrackingService.getAllLocations());
    }

    /**
     * WebSocket: Ambulance device pushes location update
     * STOMP destination: /app/ambulance/location
     * Broadcasts to: /topic/ambulance/{id}/location
     */
    @MessageMapping("/ambulance/location")
    public void handleWsLocationUpdate(@Payload Map<String, Object> payload) {
        Long id    = Long.parseLong(payload.get("ambulanceId").toString());
        double lat = Double.parseDouble(payload.get("latitude").toString());
        double lng = Double.parseDouble(payload.get("longitude").toString());
        double spd = payload.containsKey("speed") ? Double.parseDouble(payload.get("speed").toString()) : 0;
        String hdg = payload.getOrDefault("heading", "N").toString();
        gpsTrackingService.updateLocation(id, lat, lng, spd, hdg);
    }
}
