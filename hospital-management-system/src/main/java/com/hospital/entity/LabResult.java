package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "lab_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabResult {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id")
    private LabOrder labOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id")
    private LabTest labTest;

    @Column(name = "result_value", columnDefinition = "TEXT")
    private String resultValue;

    @Column(name = "reference_range", length = 100)
    private String referenceRange;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "result_document_url", length = 500)
    private String resultDocumentUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by")
    private User enteredBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
