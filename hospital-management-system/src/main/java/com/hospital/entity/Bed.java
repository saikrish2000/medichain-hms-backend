package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "beds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bed_number", nullable = false, length = 20)
    private String bedNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedStatus status = BedStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_patient_id")
    private Patient currentPatient;

    @Column(name = "admitted_at")
    private LocalDateTime admittedAt;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp   @Column(name = "updated_at")                    private LocalDateTime updatedAt;

    public enum BedStatus { AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE }
}
