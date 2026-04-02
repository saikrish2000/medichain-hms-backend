package com.hospital.entity;

import com.hospital.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", unique = true, nullable = false, length = 30)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    // Requested by — patient OR hospital user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 5)
    private BloodGroup bloodGroup;

    @Column(name = "units_requested")
    private Integer unitsRequested;

    @Column(name = "units_approved")
    private Integer unitsApproved;

    @Enumerated(EnumType.STRING)
    @Column(name = "requester_type")
    private RequesterType requesterType = RequesterType.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "patient_name", length = 100)
    private String patientName;

    @Column(name = "reason", columnDefinition = "TEXT")
    @Column(name = "urgency_level", length = 50)
    private String urgencyLevel = "NORMAL";
    private String reason;

    @Column(name = "hospital_name", length = 200)
    private String hospitalName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "is_emergency")
    private Boolean isEmergency = false;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RequesterType { INDIVIDUAL, HOSPITAL_INTERNAL, HOSPITAL_EXTERNAL }
    public enum RequestStatus  { PENDING, APPROVED, REJECTED, FULFILLED, CANCELLED }
}
