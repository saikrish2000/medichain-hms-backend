package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "ambulances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ambulance {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", unique = true, nullable = false, length = 30)
    private String vehicleNumber;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "ambulance_type", length = 30)
    private String ambulanceType;

    @Column(name = "status", length = 20)
    private String status = "AVAILABLE";

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    @Column(name = "current_latitude", precision = 10, scale = 8)
    private java.math.BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 11, scale = 8)
    private java.math.BigDecimal currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private HospitalBranch branch;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
