package com.hospital.repository;

import com.hospital.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Page<Invoice> findByPatientId(Long patientId, Pageable pageable);
    Page<Invoice> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
    long countByPatientIdAndStatus(Long patientId, String status);
}
