package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "test_code", unique = true, length = 30)
    private String testCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TestCategory category;

    @Column(name = "normal_range", length = 100)
    private String normalRange;

    @Column(length = 50)
    private String unit;

    @Column(name = "sample_type", length = 50)
    private String sampleType;    // Blood, Urine, Stool, Swab, etc.

    @Column(name = "turnaround_hours")
    private Integer turnaroundHours = 24;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private HospitalBranch branch;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp   @Column(name = "updated_at")                    private LocalDateTime updatedAt;

    public enum TestCategory {
        HAEMATOLOGY, BIOCHEMISTRY, MICROBIOLOGY, IMMUNOLOGY,
        PATHOLOGY, RADIOLOGY, URINE_ANALYSIS, OTHER
    }
}
