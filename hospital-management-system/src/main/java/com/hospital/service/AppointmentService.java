package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.entity.Appointment.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository  appointmentRepo;
    private final PatientRepository      patientRepo;
    private final DoctorRepository       doctorRepo;
    private final DoctorSlotRepository   slotRepo;
    private final DepartmentRepository   deptRepo;
    private final HospitalBranchRepository branchRepo;
    private final NotificationService    notificationService;
    private final AuditService           auditService;

    private static final AtomicLong SEQ = new AtomicLong(1000);

    // ── BOOK APPOINTMENT (by patient) ─────────────────────
    @Transactional
    public Appointment bookAppointment(Long patientUserId, Long doctorId,
                                       LocalDate date, LocalTime time,
                                       Long slotId, String symptoms,
                                       AppointmentType type) {
        Patient patient = patientRepo.findByUserId(patientUserId)
            .orElseThrow(() -> new BadRequestException("Patient profile not found."));

        Doctor doctor = doctorRepo.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));

        if (doctor.getApprovalStatus() != Doctor.ApprovalStatus.APPROVED)
            throw new BadRequestException("Doctor is not available for booking.");

        // Conflict check
        if (appointmentRepo.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctorId, date, time, AppointmentStatus.CANCELLED))
            throw new BadRequestException("This slot is already booked. Please pick another time.");

        DoctorSlot slot = slotId != null ? slotRepo.findById(slotId).orElse(null) : null;

        String apptNum = "APT-" + LocalDate.now().getYear() + "-"
                       + String.format("%05d", SEQ.getAndIncrement());

        Appointment appt = Appointment.builder()
            .appointmentNumber(apptNum)
            .patient(patient)
            .doctor(doctor)
            .department(doctor.getDepartment())
            .branch(doctor.getDepartment().getBranch())
            .slot(slot)
            .appointmentDate(date)
            .appointmentTime(time)
            .type(type)
            .status(AppointmentStatus.PENDING)
            .symptoms(symptoms)
            .consultationFee(doctor.getConsultationFee())
            .isPaid(false)
            .build();

        Appointment saved = appointmentRepo.save(appt);

        // Notify doctor
        notificationService.sendAppointmentRequestToDoctor(
            doctor.getUser().getEmail(), doctor.getUser().getFullName(),
            patient.getUser().getFullName(), date, time);

        // Confirm to patient
        notificationService.sendAppointmentConfirmationToPatient(
            patient.getUser().getEmail(), patient.getUser().getFullName(),
            apptNum, doctor.getUser().getFullName(), date, time);

        return saved;
    }

    // ── CONFIRM (by doctor) ───────────────────────────────
    @Transactional
    public void confirmAppointment(Long apptId, String notes, UserPrincipal doctor) {
        Appointment appt = getAndValidate(apptId);
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.setDoctorNotes(notes);
        appt.setApprovedAt(LocalDateTime.now());
        appointmentRepo.save(appt);

        notificationService.sendAppointmentStatusUpdate(
            appt.getPatient().getUser().getEmail(),
            appt.getPatient().getUser().getFullName(),
            appt.getAppointmentNumber(), "CONFIRMED",
            appt.getAppointmentDate(), appt.getAppointmentTime());

        auditService.log(doctor.getId(), doctor.getUsername(),
            "CONFIRM_APPOINTMENT", "Appointment", apptId, null, "SUCCESS");
    }

    // ── REJECT (by doctor) ────────────────────────────────
    @Transactional
    public void rejectAppointment(Long apptId, String reason, UserPrincipal doctor) {
        Appointment appt = getAndValidate(apptId);
        appt.setStatus(AppointmentStatus.REJECTED);
        appt.setRejectionReason(reason);
        appointmentRepo.save(appt);

        notificationService.sendAppointmentStatusUpdate(
            appt.getPatient().getUser().getEmail(),
            appt.getPatient().getUser().getFullName(),
            appt.getAppointmentNumber(), "REJECTED",
            appt.getAppointmentDate(), appt.getAppointmentTime());
    }

    // ── CANCEL (by patient) ───────────────────────────────
    @Transactional
    public void cancelAppointment(Long apptId, Long patientUserId) {
        Appointment appt = getAndValidate(apptId);
        if (!appt.getPatient().getUser().getId().equals(patientUserId))
            throw new BadRequestException("Not your appointment.");
        if (appt.getStatus() == AppointmentStatus.COMPLETED)
            throw new BadRequestException("Cannot cancel a completed appointment.");
        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(appt);
    }

    // ── COMPLETE (by doctor) ──────────────────────────────
    @Transactional
    public void completeAppointment(Long apptId, UserPrincipal doctor) {
        Appointment appt = getAndValidate(apptId);
        appt.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepo.save(appt);
    }

    // ── QUERIES ───────────────────────────────────────────
    public Page<Appointment> getDoctorAppointments(Long doctorId, int page) {
        return appointmentRepo.findByDoctorId(
            doctorId, PageRequest.of(page, 15, Sort.by("appointmentDate", "appointmentTime")));
    }

    public Page<Appointment> getDoctorPendingAppointments(Long doctorId, int page) {
        return appointmentRepo.findByDoctorIdAndStatus(
            doctorId, AppointmentStatus.PENDING,
            PageRequest.of(page, 15, Sort.by("appointmentDate", "appointmentTime")));
    }

    public List<Appointment> getDoctorTodayAppointments(Long doctorId) {
        return appointmentRepo
            .findByDoctorIdAndAppointmentDateOrderByAppointmentTime(doctorId, LocalDate.now());
    }

    public Page<Appointment> getPatientAppointments(Long patientId, int page) {
        return appointmentRepo.findByPatientId(
            patientId, PageRequest.of(page, 10, Sort.by("appointmentDate").descending()));
    }

    public Page<Appointment> getAllPendingAppointments(int page) {
        return appointmentRepo.findAllPending(PageRequest.of(page, 15));
    }

    public Appointment getAppointment(Long id) {
        return appointmentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
    }

    // ── PRIVATE HELPERS ───────────────────────────────────
    private Appointment getAndValidate(Long apptId) {
        return appointmentRepo.findById(apptId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", apptId));
    }
}
