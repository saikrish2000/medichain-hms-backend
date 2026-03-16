package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "organ_donors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrganDonor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "donor_status")
    private DonorStatus donorStatus = DonorStatus.REGISTERED;

    // Comma-separated organ list stored as TEXT; easier than join table for display
    @Column(name = "organs_to_donate", columnDefinition = "TEXT")
    private String organsToDonate;  // e.g. "HEART,KIDNEY,LIVER"

    @Column(name = "donor_card_number", unique = true, length = 30)
    private String donorCardNumber;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "date_of_death")
    private LocalDate dateOfDeath;

    @Column(name = "cause_of_death", length = 200)
    private String causeOfDeath;

    @Column(name = "hospital_where_deceased", length = 200)
    private String hospitalWhereDeceased;

    @Column(name = "next_of_kin_name", length = 100)
    private String nextOfKinName;

    @Column(name = "next_of_kin_phone", length = 20)
    private String nextOfKinPhone;

    @Column(name = "next_of_kin_consent")
    private Boolean nextOfKinConsent = false;

    @Column(name = "medical_notes", columnDefinition = "TEXT")
    private String medicalNotes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DonorStatus {
        REGISTERED,   // alive, registered intent
        ACTIVE,       // verified, card issued
        DECEASED,     // deceased, organs available
        DONATED,      // organs donated
        WITHDRAWN     // withdrew consent
    }

    public enum Organ {
        HEART, KIDNEY, LIVER, LUNGS, PANCREAS, INTESTINE,
        CORNEAS, SKIN, BONE, BONE_MARROW, HEART_VALVE
    }
}
