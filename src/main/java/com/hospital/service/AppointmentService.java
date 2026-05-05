package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
            .orElseThrow(() -> new BadRequestException("Patient profile not found."));

        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot","id",slotId));

        if (Boolean.TRUE.equals(slot.getIsBlocked()))
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
                appt.getAppointmentDate(),
                appt.getAppointmentTime());
        } catch (Exception ignored) {}

        return appt;
    }

    /** Book appointment on behalf of patient (by receptionist) */
    @Transactional
    public Appointment bookByReceptionist(Map<String,Object> body) {
        Long patientId = Long.parseLong(body.get("patientId").toString());
        Long slotId    = Long.parseLong(body.get("slotId").toString());
        String reason  = (String) body.getOrDefault("reason","Walk-in");
        String notes   = (String) body.getOrDefault("notes","");

        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient","id",patientId));
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Slot","id",slotId));

        if (Boolean.TRUE.equals(slot.getIsBlocked()))
            throw new BadRequestException("Slot is blocked.");
        if (slot.getCurrentPatients() >= slot.getMaxPatients())
            throw new BadRequestException("Slot is fully booked.");

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(slot.getDoctor());
        appt.setSlot(slot);
        appt.setAppointmentDate(slot.getSlotDate());
        appt.setAppointmentTime(slot.getStartTime());
        appt.setReasonForVisit(reason);
        appt.setNotes(notes);
        appt.setStatus("CONFIRMED"); // confirmed directly when booked by receptionist
        appt.setCreatedAt(LocalDateTime.now());
        appointmentRepo.save(appt);

        slot.setCurrentPatients(slot.getCurrentPatients() + 1);
        slotRepo.save(slot);
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
                "#" + appt.getId(),
                appt.getDoctor().getUser().getFullName(),
                appt.getAppointmentDate(),
                appt.getAppointmentTime());
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
        if ("COMPLETED".equals(appt.getStatus()) || "CANCELLED".equals(appt.getStatus()))
            throw new BadRequestException("Cannot cancel a " + appt.getStatus().toLowerCase() + " appointment");
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
            PageRequest.of(page,15,Sort.by("appointmentDate").descending()));
    }

    public Page<Appointment> getDoctorPendingAppointments(Long doctorId, int page) {
        return appointmentRepo.findByDoctorIdAndStatus(doctorId,"PENDING",
            PageRequest.of(page,15,Sort.by("appointmentDate")));
    }

    public Page<Appointment> getDoctorAllAppointments(Long doctorId, int page) {
        return appointmentRepo.findByDoctorId(doctorId,
            PageRequest.of(page,15,Sort.by("appointmentDate").descending()));
    }

    public List<Appointment> getTodayAppointments(Long doctorId) {
        return appointmentRepo.findByDoctorIdAndAppointmentDate(doctorId, LocalDate.now());
    }

    public Appointment getNextAppointment(Long patientId) {
        List<Appointment> list = appointmentRepo.findByPatientIdAndStatusIn(
            patientId, List.of("PENDING","CONFIRMED"));
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
