package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorSlotRepository  slotRepo;
    private final PatientRepository     patientRepo;
    private final DoctorRepository      doctorRepo;
    private final NotificationService   notificationService;

    @Transactional
    public Appointment bookAppointment(Long patientUserId, Long slotId,
                                       String reason, String notes) {
        Patient patient = patientRepo.findByUserId(patientUserId)
            .orElseThrow(() -> new BadRequestException("Patient profile not found. Please complete your profile first."));

        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot","id",slotId));

        if (slot.getIsBlocked())
            throw new BadRequestException("This slot is no longer available.");
        if (slot.getCurrentPatients() >= slot.getMaxPatients())
            throw new BadRequestException("This slot is fully booked.");

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(slot.getDoctor());
        appt.setSlot(slot);
        appt.setAppointmentDate(slot.getSlotDate());
        appt.setAppointmentTime(slot.getStartTime());
        appt.setReasonForVisit(reason);
        appt.setNotes(notes);
        appt.setStatus("PENDING");
        appt.setCreatedAt(LocalDateTime.now());
        appointmentRepo.save(appt);

        slot.setCurrentPatients(slot.getCurrentPatients() + 1);
        slotRepo.save(slot);

        try {
            notificationService.sendAppointmentRequestToDoctor(
                slot.getDoctor().getUser().getEmail(),
                slot.getDoctor().getUser().getFullName(),
                patient.getUser().getFullName(),
                appt.getAppointmentDate().toString());
        } catch (Exception ignored) {}

        return appt;
    }

    @Transactional
    public void confirmAppointment(Long appointmentId, Long doctorUserId) {
        Appointment appt = getByIdAndValidateDoctor(appointmentId, doctorUserId);
        appt.setStatus("CONFIRMED");
        appointmentRepo.save(appt);
        try {
            notificationService.sendAppointmentConfirmationToPatient(
                appt.getPatient().getUser().getEmail(),
                appt.getPatient().getUser().getFullName(),
                appt.getDoctor().getUser().getFullName(),
                appt.getAppointmentDate().toString());
        } catch (Exception ignored) {}
    }

    @Transactional
    public void rejectAppointment(Long appointmentId, Long doctorUserId, String reason) {
        Appointment appt = getByIdAndValidateDoctor(appointmentId, doctorUserId);
        appt.setStatus("REJECTED");
        appt.setRejectionReason(reason);
        appointmentRepo.save(appt);
    }

    @Transactional
    public void completeAppointment(Long appointmentId, Long doctorUserId) {
        Appointment appt = getByIdAndValidateDoctor(appointmentId, doctorUserId);
        appt.setStatus("COMPLETED");
        appt.setCompletedAt(LocalDateTime.now());
        appointmentRepo.save(appt);
    }

    @Transactional
    public void markNoShow(Long appointmentId, Long doctorUserId) {
        Appointment appt = getByIdAndValidateDoctor(appointmentId, doctorUserId);
        appt.setStatus("NO_SHOW");
        appointmentRepo.save(appt);
    }

    @Transactional
    public void cancelByPatient(Long appointmentId, Long patientUserId) {
        Appointment appt = appointmentRepo.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment","id",appointmentId));
        Patient patient = patientRepo.findByUserId(patientUserId)
            .orElseThrow(() -> new BadRequestException("Patient not found"));
        if (!appt.getPatient().getId().equals(patient.getId()))
            throw new BadRequestException("Not authorised to cancel this appointment");
        if (appt.getStatus().equals("COMPLETED") ||
            appt.getStatus().equals("CANCELLED"))
            throw new BadRequestException("Cannot cancel this appointment");
        appt.setStatus("CANCELLED");
        appointmentRepo.save(appt);
        DoctorSlot slot = appt.getSlot();
        if (slot != null) {
            slot.setCurrentPatients(Math.max(0, slot.getCurrentPatients() - 1));
            slotRepo.save(slot);
        }
    }

    public Page<Appointment> getPatientAppointments(Long patientId, int page) {
        return appointmentRepo.findByPatientId(patientId,
            PageRequest.of(page, 15, Sort.by("appointmentDate").descending()));
    }

    public Page<Appointment> getDoctorPendingAppointments(Long doctorId, int page) {
        return appointmentRepo.findByDoctorIdAndStatus(doctorId, "PENDING",
            PageRequest.of(page, 15, Sort.by("appointmentDate")));
    }

    public Page<Appointment> getDoctorAllAppointments(Long doctorId, int page) {
        return appointmentRepo.findByDoctorId(doctorId,
            PageRequest.of(page, 15, Sort.by("appointmentDate").descending()));
    }

    public List<Appointment> getTodayAppointments(Long doctorId) {
        return appointmentRepo.findByDoctorIdAndAppointmentDateOrderByAppointmentTime(
            doctorId, java.time.LocalDate.now());
    }

    public Appointment getNextAppointment(Long patientId) {
        List<Appointment> list = appointmentRepo.findNextAppointments(
            patientId, PageRequest.of(0,1));
        return list.isEmpty() ? null : list.get(0);
    }

    public Appointment getById(Long id) {
        return appointmentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment","id",id));
    }

    private Appointment getByIdAndValidateDoctor(Long appointmentId, Long doctorUserId) {
        Appointment appt = appointmentRepo.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment","id",appointmentId));
        Doctor doctor = doctorRepo.findByUserId(doctorUserId)
            .orElseThrow(() -> new BadRequestException("Doctor not found"));
        if (!appt.getDoctor().getId().equals(doctor.getId()))
            throw new BadRequestException("Not authorised to manage this appointment");
        return appt;
    }
}
