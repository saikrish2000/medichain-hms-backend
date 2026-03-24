package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ambulances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", unique = true, nullable = false, length = 20)
    private String vehicleNumber;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType; // Basic, Advanced Life Support, Neonatal

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmbulanceStatus status = AmbulanceStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private HospitalBranch branch;

    @Column(name = "current_latitude", precision = 10, scale = 8)
    private Double currentLatitude;

    @Column(name = "current_longitude", precision = 11, scale = 8)
    private Double currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp   @Column(name = "updated_at")                    private LocalDateTime updatedAt;

    public enum AmbulanceStatus { AVAILABLE, DISPATCHED, ON_ROUTE, AT_SCENE, RETURNING, MAINTENANCE }
}
