package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PharmacyService {

    private final MedicineRepository     medicineRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final PatientRepository      patientRepo;
    private final DoctorRepository       doctorRepo;
    private final AppointmentRepository  appointmentRepo;

    public Page<Medicine> searchMedicines(String q, int page) {
        if (q != null && !q.isBlank())
            return medicineRepo.search(q, PageRequest.of(page, 20));
        return medicineRepo.findAll(PageRequest.of(page, 20, Sort.by("name")));
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepo.findLowStock();
    }

    public Medicine saveMedicine(Medicine medicine) { return medicineRepo.save(medicine); }

    @Transactional
    public Medicine updateStock(Long id, int qty, String op) {
        Medicine m = medicineRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Medicine","id",id));
        int cur = m.getCurrentStock();
        switch (op) {
            case "add"      -> m.setCurrentStock(cur + qty);
            case "subtract" -> {
                if (cur < qty) throw new BadRequestException("Insufficient stock");
                m.setCurrentStock(cur - qty);
            }
            default         -> m.setCurrentStock(qty);
        }
        return medicineRepo.save(m);
    }

    @Transactional
    public Prescription createPrescription(Long patientId, Long doctorId,
                                           Long appointmentId, String notes,
                                           List<Map<String,Object>> items) {
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient","id",patientId));
        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor","id",doctorId));
        Prescription rx = new Prescription();
        rx.setPatient(patient);
        rx.setDoctor(doctor);
        rx.setNotes(notes);
        rx.setStatus(Prescription.Status.PENDING);
        rx.setCreatedAt(LocalDateTime.now());
        if (appointmentId != null)
            appointmentRepo.findById(appointmentId).ifPresent(rx::setAppointment);
        List<PrescriptionItem> rxItems = new ArrayList<>();
        if (items != null) {
            for (Map<String,Object> item : items) {
                PrescriptionItem pi = new PrescriptionItem();
                Long medId = Long.parseLong(item.get("medicineId").toString());
                Medicine med = medicineRepo.findById(medId)
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine","id",medId));
                pi.setMedicine(med);
                pi.setDosage((String) item.get("dosage"));
                pi.setFrequency((String) item.get("frequency"));
                pi.setDuration((String) item.get("duration"));
                pi.setInstructions((String) item.getOrDefault("instructions",""));
                pi.setPrescription(rx);
                rxItems.add(pi);
            }
        }
        rx.setItems(rxItems);
        return prescriptionRepo.save(rx);
    }

    @Transactional
    public Prescription dispensePrescription(Long id, UserPrincipal pharmacist) {
        Prescription rx = prescriptionRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prescription","id",id));
        if (rx.getStatus() == Prescription.Status.DISPENSED)
            throw new BadRequestException("Already dispensed");
        for (PrescriptionItem item : rx.getItems()) {
            Medicine med = item.getMedicine();
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            if (med.getCurrentStock() >= qty) {
                med.setCurrentStock(med.getCurrentStock() - qty);
                medicineRepo.save(med);
            }
        }
        rx.setStatus(Prescription.Status.DISPENSED);
        rx.setDispensedAt(LocalDateTime.now());
        rx.setDispensedBy(pharmacist.getId());
        return prescriptionRepo.save(rx);
    }

    public Page<Prescription> getPendingPrescriptions(int page) {
        return prescriptionRepo.findByStatus(Prescription.Status.PENDING,
            PageRequest.of(page, 20, Sort.by("createdAt").descending()));
    }

    public Map<String,Object> getDashboardStats() {
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("totalMedicines",    medicineRepo.count());
        s.put("lowStockCount",     medicineRepo.countLowStock());
        s.put("pendingPrescriptions", prescriptionRepo.countByStatus(Prescription.Status.PENDING));
        return s;
    }
}
