package com.hospital.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GpsTrackingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<Long, Map<String, Object>> locationStore = new ConcurrentHashMap<>();

    public void updateLocation(Long ambulanceId, double lat, double lng, double speed, String heading) {
        Map<String, Object> loc = new LinkedHashMap<>();
        loc.put("ambulanceId", ambulanceId);
        loc.put("latitude", lat); loc.put("longitude", lng);
        loc.put("speed", speed); loc.put("heading", heading);
        loc.put("timestamp", LocalDateTime.now().toString());
        locationStore.put(ambulanceId, loc);
        messagingTemplate.convertAndSend("/topic/ambulance/" + ambulanceId + "/location", loc);
    }

    public Map<String, Object> getLastLocation(Long ambulanceId) {
        return locationStore.getOrDefault(ambulanceId, Map.of("ambulanceId", ambulanceId, "status", "NO_DATA"));
    }

    public Map<Long, Map<String, Object>> getAllLocations() { return Map.copyOf(locationStore); }

    public void broadcastCall(Long callId, String callerName, String location, String emergencyType, String priority) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("callId", callId); data.put("callerName", callerName);
        data.put("location", location); data.put("emergencyType", emergencyType);
        data.put("priority", priority); data.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/dispatch/new-call", data);
    }

    public void broadcastDispatch(Long callId, Long ambulanceId, String vehicleNumber, String driverName) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("callId", callId); data.put("ambulanceId", ambulanceId);
        data.put("vehicleNumber", vehicleNumber); data.put("driverName", driverName);
        data.put("status", "DISPATCHED"); data.put("timestamp", LocalDateTime.now().toString());
        messagingTemplate.convertAndSend("/topic/dispatch/status", data);
        messagingTemplate.convertAndSend("/topic/call/" + callId + "/status", data);
    }
}
