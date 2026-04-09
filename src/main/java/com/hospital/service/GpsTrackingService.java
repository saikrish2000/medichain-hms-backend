package com.hospital.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsTrackingService {

    private final SimpMessagingTemplate messagingTemplate;

    // In-memory store: ambulanceId → last known location
    private final ConcurrentHashMap<Long, Map<String, Object>> locationStore = new ConcurrentHashMap<>();

    /**
     * Update ambulance GPS location and broadcast to subscribers.
     * Endpoint: /app/ambulance/location
     * Broadcast: /topic/ambulance/{id}/location
     */
    public void updateLocation(Long ambulanceId, double latitude, double longitude,
                                double speed, String heading) {
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("ambulanceId", ambulanceId);
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        location.put("speed", speed);
        location.put("heading", heading);
        location.put("timestamp", LocalDateTime.now().toString());

        locationStore.put(ambulanceId, location);

        // Broadcast to all subscribers of this ambulance's location topic
        messagingTemplate.convertAndSend("/topic/ambulance/" + ambulanceId + "/location", location);
        log.debug("GPS update broadcast → ambulance {} at ({}, {})", ambulanceId, latitude, longitude);
    }

    /** Get last known location of an ambulance */
    public Map<String, Object> getLastLocation(Long ambulanceId) {
        return locationStore.getOrDefault(ambulanceId, Map.of("ambulanceId", ambulanceId, "status", "NO_DATA"));
    }

    /** Get all active ambulance locations */
    public Map<Long, Map<String, Object>> getAllLocations() {
        return Map.copyOf(locationStore);
    }

    /** Broadcast active ambulance call to dispatch room */
    public void broadcastCall(Long callId, String callerName, String location,
                               String emergencyType, String priority) {
        Map<String, Object> callData = new LinkedHashMap<>();
        callData.put("callId", callId);
        callData.put("callerName", callerName);
        callData.put("location", location);
        callData.put("emergencyType", emergencyType);
        callData.put("priority", priority);
        callData.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/dispatch/new-call", callData);
        log.info("New call broadcast to dispatch → callId: {}", callId);
    }

    /** Notify when ambulance is dispatched */
    public void broadcastDispatch(Long callId, Long ambulanceId, String vehicleNumber,
                                   String driverName) {
        Map<String, Object> dispatch = new LinkedHashMap<>();
        dispatch.put("callId", callId);
        dispatch.put("ambulanceId", ambulanceId);
        dispatch.put("vehicleNumber", vehicleNumber);
        dispatch.put("driverName", driverName);
        dispatch.put("status", "DISPATCHED");
        dispatch.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/dispatch/status", dispatch);
        messagingTemplate.convertAndSend("/topic/call/" + callId + "/status", dispatch);
    }
}
