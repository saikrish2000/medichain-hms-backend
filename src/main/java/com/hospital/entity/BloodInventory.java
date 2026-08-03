package com.hospital.entity;

import com.hospital.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "blood_inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BloodInventory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private BloodBank bank;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 15)
    private BloodGroup bloodGroup;

    @Column(name = "units_available")
    private Integer unitsAvailable = 0;

    @Column(name = "minimum_threshold")
    private Integer minimumThreshold = 5;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt   = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { lastUpdated = LocalDateTime.now(); }

    /** Alias for compatibility */
    public Integer getUnitsInStock() { return unitsAvailable; }
    public boolean isBelowThreshold() {
        return unitsAvailable != null && minimumThreshold != null
               && unitsAvailable <= minimumThreshold;
    }
}
