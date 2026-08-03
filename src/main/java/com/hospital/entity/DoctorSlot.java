package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name = "doctor_slots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorSlot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "slot_date")
    private LocalDate slotDate;

    // stored as VARCHAR — day name (MONDAY etc.)
    @Column(name = "day_of_week", length = 10)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30;

    @Column(name = "max_patients")
    private Integer maxPatients = 1;

    /** booked_count in DB — keep both mapped */
    @Column(name = "booked_count")
    private Integer bookedCount = 0;

    @Column(name = "current_patients")
    private Integer currentPatients = 0;

    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "block_reason", length = 255)
    private String blockReason;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private HospitalBranch branch;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookedCount == null) bookedCount = 0;
        if (currentPatients == null) currentPatients = 0;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Convenience alias
    public Integer getDate() { return slotDate != null ? slotDate.getDayOfMonth() : null; }
    public LocalDate getDate2() { return slotDate; }
}
