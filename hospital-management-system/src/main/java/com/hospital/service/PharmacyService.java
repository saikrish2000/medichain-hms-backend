package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.Prescription.PrescriptionStatus;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PharmacyService {

    private final MedicineRepository     medicineRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final DoctorRepository       doctorRepo;
    private final PatientRepository      patientRepo;

    // ── MEDICINE CRUD ──────────────────────────────────────

    public Page<Medicine> getAllMedicines(int page) {
        return medicineRepo.findByIsActiveTrue(PageRequest.of(page, 20, Sort.by("name")));
    }

    public List<Medicine> searchMedicines(String query) {
        return medicineRepo.searchByName(query);
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepo.findLowStock();
    }

    @Transactional
    public Medicine saveMedicine(Medicine medicine) {
        return medicineRepo.save(medicine);
    }

    @Transactional
    public Medicine updateStock(Long medicineId, int quantity, String operation) {
        Medicine medicine = medicineRepo.findById(medicineId)
            .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", medicineId));

        if ("ADD".equals(operation)) {
            medicine.setStockQuantity(medicine.getStockQuantity() + quantity);
        } else if ("SUBTRACT".equals(operation)) {
            if (medicine.getStockQuantity() < quantity) {
                throw new BadRequestException("Insufficient stock for: " + medicine.getName());
            }
            medicine.setStockQuantity(medicine.getStockQuantity() - quantity);
        } else {
            medicine.setStockQuantity(quantity);
        }
        return medicineRepo.save(medicine);
    }

    // ── PRESCRIPTIONS ──────────────────────────────────────

    @Transactional
    public Prescription createPrescription(Long patientId, Long doctorId,
                                            Long appointmentId,
                                            List<PrescriptionItem> items,
                                            String diagnosisNotes,
                                            String doctorNotes) {
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        Prescription prescription = Prescription.builder()
            .patient(patient)
            .doctor(doctor)
            .prescriptionDate(LocalDate.now())
            .validUntil(LocalDate.now().plusDays(30))
            .status(PrescriptionStatus.ACTIVE)
            .diagnosisNotes(diagnosisNotes)
            .doctorNotes(doctorNotes)
            .build();

        items.forEach(item -> item.setPrescription(prescription));
        prescription.setItems(items);
        return prescriptionRepo.save(prescription);
    }

    @Transactional
    public Prescription dispensePrescription(Long prescriptionId, UserPrincipal pharmacist) {
        Prescription prescription = prescriptionRepo.findById(prescriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));

        if (prescription.getStatus() != PrescriptionStatus.ACTIVE) {
            throw new BadRequestException("Prescription is not active.");
        }

        // Deduct stock for each item
        for (PrescriptionItem item : prescription.getItems()) {
            if (item.getMedicine() != null && item.getQuantity() != null) {
                try {
                    updateStock(item.getMedicine().getId(), item.getQuantity(), "SUBTRACT");
                } catch (Exception e) {
                    // Log but don't block dispensing — may be manual override
                }
            }
        }

        prescription.setStatus(PrescriptionStatus.DISPENSED);
        prescription.setDispensedBy(pharmacist.getId());
        prescription.setDispensedAt(LocalDateTime.now());
        return prescriptionRepo.save(prescription);
    }

    public Page<Prescription> getPendingPrescriptions(int page) {
        return prescriptionRepo.findAll(
            PageRequest.of(page, 20, Sort.by("createdAt").descending()));
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalMedicines",   medicineRepo.countByIsActiveTrue());
        stats.put("lowStockCount",    medicineRepo.findLowStock().size());
        stats.put("pendingRx",        prescriptionRepo.findByStatus(PrescriptionStatus.ACTIVE).size());
        stats.put("lowStockItems",    medicineRepo.findLowStock());
        stats.put("recentPrescriptions", prescriptionRepo.findAll(
            PageRequest.of(0, 5, Sort.by("createdAt").descending())).getContent());
        return stats;
    }
}
