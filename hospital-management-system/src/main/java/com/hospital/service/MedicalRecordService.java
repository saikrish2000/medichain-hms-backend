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

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepo;
    private final PatientRepository       patientRepo;
    private final DoctorRepository        doctorRepo;
    private final AppointmentRepository   apptRepo;

    @Transactional
    public MedicalRecord createRecord(Long doctorUserId, Long patientId,
                                       Long appointmentId, MedicalRecord form) {
        Doctor doctor = doctorRepo.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", doctorUserId));
        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));
        Appointment appt = appointmentId != null
            ? apptRepo.findById(appointmentId).orElse(null) : null;

        MedicalRecord record = MedicalRecord.builder()
            .patient(patient)
            .doctor(doctor)
            .appointment(appt)
            .visitDate(form.getVisitDate() != null ? form.getVisitDate() : LocalDate.now())
            .diagnosis(form.getDiagnosis())
            .symptoms(form.getSymptoms())
            .treatment(form.getTreatment())
            .prescription(form.getPrescription())
            .followUpDate(form.getFollowUpDate())
            .followUpNotes(form.getFollowUpNotes())
            .bloodPressure(form.getBloodPressure())
            .heartRate(form.getHeartRate())
            .temperature(form.getTemperature())
            .weight(form.getWeight())
            .height(form.getHeight())
            .oxygenSaturation(form.getOxygenSaturation())
            .isConfidential(form.getIsConfidential() != null && form.getIsConfidential())
            .build();

        return recordRepo.save(record);
    }

    public Page<MedicalRecord> getPatientRecords(Long patientId, int page) {
        return recordRepo.findByPatientIdOrderByVisitDateDesc(
            patientId, PageRequest.of(page, 10));
    }

    public MedicalRecord getRecord(Long id) {
        return recordRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));
    }

    @Transactional
    public MedicalRecord updateRecord(Long id, MedicalRecord form) {
        MedicalRecord r = getRecord(id);
        r.setDiagnosis(form.getDiagnosis());
        r.setSymptoms(form.getSymptoms());
        r.setTreatment(form.getTreatment());
        r.setPrescription(form.getPrescription());
        r.setFollowUpDate(form.getFollowUpDate());
        r.setFollowUpNotes(form.getFollowUpNotes());
        r.setBloodPressure(form.getBloodPressure());
        r.setHeartRate(form.getHeartRate());
        r.setTemperature(form.getTemperature());
        r.setWeight(form.getWeight());
        r.setHeight(form.getHeight());
        r.setOxygenSaturation(form.getOxygenSaturation());
        return recordRepo.save(r);
    }
}
