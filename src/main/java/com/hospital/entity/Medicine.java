package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Table(name = "medicines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medicine {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity_in_stock")
    private Integer quantityInStock = 0;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "is_prescription_required")
    private Boolean isPrescriptionRequired = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (price == null && unitPrice != null) price = unitPrice;
        if (unitPrice == null && price != null) unitPrice = price;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (price == null && unitPrice != null) price = unitPrice;
    }

    /** Convenience alias */
    public Integer getStockQuantity() { return quantityInStock; }
    public Integer getReorderLevel()  { return minStockLevel; }
}
