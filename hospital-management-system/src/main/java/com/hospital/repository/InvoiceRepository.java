package com.hospital.repository;

import com.hospital.entity.Invoice;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);
    long countByStatus(Invoice.PaymentStatus status);
    long countByPatientIdAndStatus(Long patientId, Invoice.PaymentStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = 'PAID'")
    Optional<BigDecimal> sumPaidAmount();
}
