package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "organ_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrganRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "organ_needed", length = 100)
    private String organNeeded;

    @Column(name = "urgency_level", length = 20)
    private String urgencyLevel = "HIGH";

    @Column(name = "medical_justification", columnDefinition = "TEXT")
    private String medicalJustification;

    @Column(name = "status", length = 20)
    private String status = "WAITING"; // WAITING, MATCHED, TRANSPLANTED, CANCELLED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
