package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "invoice_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(name = "item_type", length = 50)
    private String itemType;    // CONSULTATION, LAB, MEDICINE, PROCEDURE

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column
    private Integer quantity = 1;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
