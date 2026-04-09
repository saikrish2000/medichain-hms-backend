package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository   appointmentRepo;
    private final NotificationService     notificationService;

    /**
     * Runs every day at 8:00 AM IST.
     * Sends reminders for all CONFIRMED appointments scheduled for tomorrow.
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Kolkata")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Running appointment reminder job for: {}", tomorrow);

        List<Appointment> appointments = appointmentRepo
                .findByAppointmentDateAndStatus(tomorrow, "CONFIRMED");

        int sent = 0;
        for (Appointment appt : appointments) {
            try {
                var patient = appt.getPatient();
                var doctor  = appt.getDoctor();
                if (patient == null || patient.getUser() == null) continue;

                String patientName = patient.getUser().getFirstName() + " " + patient.getUser().getLastName();
                String doctorName  = doctor != null ? doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName() : "Doctor";
                String email       = patient.getUser().getEmail();
                String phone       = patient.getUser().getPhone();
                String dateTime    = appt.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                                   + " at " + appt.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a"));

                // Email reminder
                notificationService.sendAppointmentReminder(
                        email, patientName, doctorName,
                        appt.getAppointmentNumber(),
                        appt.getAppointmentDate(), appt.getAppointmentTime());

                // SMS reminder
                if (phone != null && !phone.isBlank())
                    notificationService.sendAppointmentReminderSms(phone, patientName, doctorName, dateTime);

                sent++;
            } catch (Exception e) {
                log.error("Failed reminder for appointment {}: {}", appt.getId(), e.getMessage());
            }
        }
        log.info("Appointment reminders sent: {}/{}", sent, appointments.size());
    }
}
