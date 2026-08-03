package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "organ_donors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrganDonor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "consent_document_url", length = 500)
    private String consentDocumentUrl;

    @Column(name = "organs_to_donate", columnDefinition = "TEXT")
    private String organsToDonate;  // JSON array of organ names

    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(name = "status", length = 20)
    private String status = "REGISTERED"; // REGISTERED, DECEASED, DONATED, WITHDRAWN

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
