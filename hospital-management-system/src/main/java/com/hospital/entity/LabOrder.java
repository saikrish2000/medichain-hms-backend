package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ORDERED;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "sample_collected_at")
    private LocalDateTime sampleCollectedAt;

    @Column(name = "results_at")
    private LocalDateTime resultsAt;

    @Column(name = "processed_by")
    private Long processedBy; // Lab technician user ID

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @OneToMany(mappedBy = "labOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LabResult> results = new ArrayList<>();

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp   @Column(name = "updated_at")                    private LocalDateTime updatedAt;

    public enum OrderStatus { ORDERED, SAMPLE_COLLECTED, PROCESSING, COMPLETED, CANCELLED }
}
