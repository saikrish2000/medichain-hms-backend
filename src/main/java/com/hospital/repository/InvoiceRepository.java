package com.hospital.repository;

import com.hospital.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByPatientId(Long patientId, Pageable pageable);

    Optional<Invoice> findByTransactionId(String transactionId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(i.amountPaid), 0) FROM Invoice i WHERE i.status = 'PAID'")
    Optional<BigDecimal> sumPaidAmount();

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = 'PENDING'")
    Optional<BigDecimal> sumPendingAmount();
}
