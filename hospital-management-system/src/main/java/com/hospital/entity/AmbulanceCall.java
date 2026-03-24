package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ambulance_calls")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AmbulanceCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambulance_id")
    private Ambulance ambulance;

    @Column(name = "caller_name", nullable = false, length = 100)
    private String callerName;

    @Column(name = "caller_phone", nullable = false, length = 20)
    private String callerPhone;

    @Column(name = "pickup_address", nullable = false, columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(name = "pickup_latitude", precision = 10, scale = 8)
    private Double pickupLatitude;

    @Column(name = "pickup_longitude", precision = 11, scale = 8)
    private Double pickupLongitude;

    @Column(name = "emergency_type", length = 100)
    private String emergencyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status = CallStatus.REQUESTED;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp   @Column(name = "updated_at")                    private LocalDateTime updatedAt;

    public enum CallStatus { REQUESTED, DISPATCHED, ON_ROUTE, AT_SCENE, COMPLETED, CANCELLED }
}
