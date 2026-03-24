package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(name = "medicine_name", length = 200)
    private String medicineName; // fallback if medicine not in DB

    @Column(nullable = false, length = 100)
    private String dosage;        // "1 tablet"

    @Column(nullable = false, length = 100)
    private String frequency;     // "Twice daily"

    @Column(nullable = false, length = 50)
    private String duration;      // "7 days"

    @Column(length = 50)
    private String timing;        // "After food"

    @Column(columnDefinition = "TEXT")
    private String instructions;

    private Integer quantity;
}
