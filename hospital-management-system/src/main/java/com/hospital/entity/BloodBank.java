package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "blood_banks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BloodBank {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private HospitalBranch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_user_id")
    private User manager;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
