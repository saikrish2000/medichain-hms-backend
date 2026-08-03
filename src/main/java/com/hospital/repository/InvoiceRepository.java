package com.hospital.repository;

import com.hospital.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByPatientId(Long patientId, Pageable pageable);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByTransactionId(String transactionId);

    List<Invoice> findByStatus(String status);

    Page<Invoice> findByStatus(String status, Pageable pageable);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.patient.id = :pid AND i.status = :status")
    long countByPatientIdAndStatus(@Param("pid") Long patientId, @Param("status") String status);

    @Query("SELECT SUM(i.amountPaid) FROM Invoice i WHERE i.status = 'PAID'")
    Optional<BigDecimal> sumPaidAmount();

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'PENDING'")
    Optional<BigDecimal> sumPendingAmount();
}
