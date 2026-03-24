package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(length = 100)
    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(length = 50)
    private String dosageForm;   // Tablet, Capsule, Syrup, Injection, etc.

    @Column(length = 50)
    private String strength;     // 500mg, 10ml, etc.

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "reorder_level")
    private Integer reorderLevel = 10;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "requires_prescription")
    private Boolean requiresPrescription = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private HospitalBranch branch;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Category {
        ANTIBIOTIC, ANALGESIC, ANTIHYPERTENSIVE, ANTIDIABETIC,
        ANTACID, ANTIHISTAMINE, VITAMIN, ANTIPARASITIC,
        CARDIAC, NEUROLOGICAL, RESPIRATORY, OTHER
    }
}
