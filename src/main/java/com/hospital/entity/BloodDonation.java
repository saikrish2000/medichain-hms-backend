package com.hospital.entity;

import com.hospital.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "blood_donations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BloodDonation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id")
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private BloodBank bank;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 15)
    private BloodGroup bloodGroup;

    @Column(name = "units_donated")
    private Integer unitsDonated = 1;

    @Column(name = "donation_date")
    private LocalDate donationDate;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED

    @Column(name = "health_check_passed")
    private Boolean healthCheckPassed;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); if (donationDate == null) donationDate = LocalDate.now(); }
}
