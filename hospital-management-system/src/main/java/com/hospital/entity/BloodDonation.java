package com.hospital.entity;

import com.hospital.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_donations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BloodDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 5)
    private BloodGroup bloodGroup;

    @Column(name = "units_donated")
    private Integer unitsDonated = 1;

    @Column(name = "donation_date", nullable = false)
    private LocalDate donationDate;

    @Column(name = "next_eligible_date")
    private LocalDate nextEligibleDate;   // 90 days after donation

    @Enumerated(EnumType.STRING)
    private DonationStatus status = DonationStatus.PENDING;

    @Column(name = "screened_by_user_id")
    private Long screenedByUserId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum DonationStatus { PENDING, SCREENED, ACCEPTED, REJECTED }
}
