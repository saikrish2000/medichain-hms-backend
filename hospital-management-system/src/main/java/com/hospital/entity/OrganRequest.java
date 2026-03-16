package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "organ_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrganRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", unique = true, nullable = false, length = 30)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_doctor_id")
    private Doctor requestingDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private HospitalBranch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "organ_needed", nullable = false, length = 30)
    private OrganDonor.Organ organNeeded;

    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel = UrgencyLevel.NORMAL;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.WAITING;

    @Column(name = "medical_justification", columnDefinition = "TEXT")
    private String medicalJustification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_donor_id")
    private OrganDonor matchedDonor;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "transplant_date")
    private LocalDate transplantDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UrgencyLevel { NORMAL, URGENT, CRITICAL }
    public enum RequestStatus { WAITING, MATCHED, TRANSPLANTED, CANCELLED }
}
