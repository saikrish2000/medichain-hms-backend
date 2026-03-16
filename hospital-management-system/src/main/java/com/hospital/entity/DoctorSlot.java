package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_slots",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"doctor_id","slot_date","start_time"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Specific date OR recurring day
    @Column(name = "slot_date")
    private LocalDate slotDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;          // for recurring slots

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration_minutes")
    private Integer slotDurationMinutes = 15; // each appointment unit

    @Column(name = "max_patients")
    private Integer maxPatients = 1;

    @Enumerated(EnumType.STRING)
    private SlotType slotType = SlotType.SPECIFIC_DATE;

    @Enumerated(EnumType.STRING)
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Column(name = "block_reason", length = 255)
    private String blockReason;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum SlotType   { SPECIFIC_DATE, RECURRING }
    public enum SlotStatus { AVAILABLE, BLOCKED, FULL }
}
