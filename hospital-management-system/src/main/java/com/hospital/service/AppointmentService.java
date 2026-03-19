package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.Appointment.AppointmentStatus;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository  appointmentRepo;
    private final DoctorSlotRepository   slotRepo;
    private final PatientRepository      patientRepo;
    private final DoctorRepository       doctorRepo;
    private final NotificationService    notificationService;

    private static final AtomicLong SEQ = new AtomicLong(1000);

    // ── BOOK ──────────────────────────────────────────────

    @Transactional
    public Appointment bookAppointment(Long patientUserId, Long slotId,
                                        String reason, String notes) {
        Patient patient = patientRepo.findByUserId(patientUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "userId", patientUserId));
        DoctorSlot slot = slotRepo.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("DoctorSlot", "id", slotId));

        if (slot.getIsBlocked())
            throw new BadRequestException("This slot is no longer available.");

        long booked = appointmentRepo.countBySlotIdAndStatus(slotId, AppointmentStatus.CONFIRMED)
                    + appointmentRepo.countBySlotIdAndStatus(slotId, AppointmentStatus.PENDING);
        if (booked >= slot.getMaxPatients())
            throw new BadRequestException("This slot is fully booked.");

        String apptNumber = "APT-" + LocalDate.now().getYear() + "-"
                           + String.format("%05d", SEQ.getAndIncrement());

        Appointment appt = Appointment.builder()
            .appointmentNumber(apptNumber)
            .patient(patient).doctor(slot.getDoctor()).slot(slot)
            .appointmentDate(slot.getSlotDate())
            .appointmentTime(slot.getSlotTime())
            .durationMinutes(slot.getDurationMinutes())
            .reasonForVisit(reason)
            .notes(notes)
            .status(AppointmentStatus.PENDING)
            .build();

        Appointment saved = appointmentRepo.save(appt);

        // Notify doctor
        notificationService.sendAppointmentRequestToDoctor(
            saved.getDoctor().getUser().getEmail(),
            saved.getDoctor().getUser().getFullName(),
            saved.getPatient().getUser().getFullName(),
            saved.getAppointmentDate(), saved.getAppointmentTime());

        // Confirm receipt to patient
        notificationService.sendAppointmentConfirmationToPatient(
            saved.getPatient().getUser().getEmail(),
            saved.getPatient().getUser().getFullName(),
            apptNumber,
            saved.getDoctor().getUser().getFullName(),
            saved.getAppointmentDate(), saved.getAppointmentTime());

        return saved;
    }

    // ── DOCTOR ACTIONS ────────────────────────────────────

    @Transactional
    public void confirmAppointment(Long appointmentId, Long doctorUserId) {
        Appointment appt = getById(appointmentId);
        validateDoctorOwnership(appt, doctorUserId);
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepo.save(appt);
        notificationService.sendAppointmentStatusUpdate(
            appt.getPatient().getUser().getEmail(),
            appt.getPatient().getUser().getFullName(),
            appt.getAppointmentNumber(), "CONFIRMED",
            appt.getAppointmentDate(), appt.getAppointmentTime());
    }

    @Transactional
    public void rejectAppointment(Long appointmentId, Long doctorUserId, String reason) {
        Appointment appt = getById(appointmentId);
        validateDoctorOwnership(appt, doctorUserId);
        appt.setStatus(AppointmentStatus.REJECTED);
        appt.setNotes("Rejected: " + reason);
        appointmentRepo.save(appt);
        notificationService.sendAppointmentStatusUpdate(
            appt.getPatient().getUser().getEmail(),
            appt.getPatient().getUser().getFullName(),
            appt.getAppointmentNumber(), "REJECTED",
            appt.getAppointmentDate(), appt.getAppointmentTime());
    }

    @Transactional
    public void completeAppointment(Long appointmentId, Long doctorUserId) {
        Appointment appt = getById(appointmentId);
        validateDoctorOwnership(appt, doctorUserId);
        appt.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepo.save(appt);
    }

    // ── PATIENT ACTIONS ───────────────────────────────────

    @Transactional
    public void cancelByPatient(Long appointmentId, Long patientUserId) {
        Appointment appt = getById(appointmentId);
        Patient patient = patientRepo.findByUserId(patientUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "userId", patientUserId));
        if (!appt.getPatient().getId().equals(patient.getId()))
            throw new BadRequestException("Not your appointment.");
        if (appt.getStatus() == AppointmentStatus.COMPLETED)
            throw new BadRequestException("Cannot cancel a completed appointment.");
        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(appt);
        notificationService.sendAppointmentStatusUpdate(
            appt.getPatient().getUser().getEmail(),
            appt.getPatient().getUser().getFullName(),
            appt.getAppointmentNumber(), "CANCELLED",
            appt.getAppointmentDate(), appt.getAppointmentTime());
    }

    // ── QUERIES ───────────────────────────────────────────

    public Page<Appointment> getPatientAppointments(Long patientId, int page) {
        return appointmentRepo.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(
            patientId, PageRequest.of(page, 10));
    }

    public Page<Appointment> getDoctorPendingAppointments(Long doctorId, int page) {
        return appointmentRepo.findByDoctorIdAndStatus(doctorId, AppointmentStatus.PENDING,
            PageRequest.of(page, 15, Sort.by("appointmentDate").ascending()
                .and(Sort.by("appointmentTime").ascending())));
    }

    public Page<Appointment> getDoctorAllAppointments(Long doctorId, int page) {
        return appointmentRepo.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(
            doctorId, PageRequest.of(page, 15));
    }

    // ── NEXT APPOINTMENT for patient dashboard ─────────────

    public Appointment getNextAppointment(Long patientId) {
        return appointmentRepo
            .findByPatientIdOrderByAppointmentDateDesc(patientId)
            .stream()
            .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED
                      || a.getStatus() == AppointmentStatus.PENDING)
            .filter(a -> !a.getAppointmentDate().isBefore(LocalDate.now()))
            .findFirst()
            .orElse(null);
    }

    // ── HELPERS ───────────────────────────────────────────

    public Appointment getById(Long id) {
        return appointmentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
    }

    private void validateDoctorOwnership(Appointment appt, Long doctorUserId) {
        Doctor doctor = appt.getDoctor();
        if (!doctor.getUser().getId().equals(doctorUserId))
            throw new BadRequestException("Not your appointment.");
    }
}
