package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id", nullable = false)
    private LabOrder labOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;

    @Column(name = "result_value", length = 200)
    private String resultValue;

    @Column(length = 50)
    private String unit;

    @Column(name = "normal_range", length = 100)
    private String normalRange;

    @Enumerated(EnumType.STRING)
    private ResultFlag flag = ResultFlag.NORMAL;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "entered_by")
    private Long enteredBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum ResultFlag { NORMAL, LOW, HIGH, CRITICAL_LOW, CRITICAL_HIGH }
}
