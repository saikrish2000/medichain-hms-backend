package com.hospital.entity;

import com.hospital.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"blood_bank_id", "blood_group"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BloodInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 5)
    private BloodGroup bloodGroup;

    @Column(name = "units_available")
    private Integer unitsAvailable = 0;

    @Column(name = "units_reserved")
    private Integer unitsReserved = 0;

    @Column(name = "minimum_threshold")
    private Integer minimumThreshold = 5;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Integer getUnitsUsable() {
        return Math.max(0, unitsAvailable - unitsReserved);
    }

    public boolean isBelowThreshold() {
        return unitsAvailable <= minimumThreshold;
    }
}
