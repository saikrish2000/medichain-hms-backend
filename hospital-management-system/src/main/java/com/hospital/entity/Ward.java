package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WardType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private HospitalBranch branch;

    @Column(name = "total_beds")
    private Integer totalBeds = 0;

    @Column(name = "daily_rate", precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "floor_number", length = 10)
    private String floorNumber;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "ward", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Bed> beds = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum WardType { GENERAL, PRIVATE, ICU, NICU, EMERGENCY, MATERNITY, ISOLATION }
}
