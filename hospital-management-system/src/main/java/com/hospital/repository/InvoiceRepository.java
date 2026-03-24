package com.hospital.repository;

import com.hospital.entity.Invoice;
import com.hospital.entity.Invoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Page<Invoice> findByPatientId(Long patientId, Pageable pageable);
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    Page<Invoice> findByPatientIdAndStatus(Long patientId, InvoiceStatus status, Pageable pageable);
    List<Invoice> findByInvoiceDateBetween(LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(i.totalAmount),0) FROM Invoice i WHERE i.invoiceDate = :date AND i.status = 'PAID'")
    BigDecimal sumPaidByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(i.totalAmount),0) FROM Invoice i WHERE i.invoiceDate BETWEEN :from AND :to AND i.status = 'PAID'")
    BigDecimal sumPaidBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByStatus(InvoiceStatus status);
    long countByInvoiceDate(LocalDate date);
}
