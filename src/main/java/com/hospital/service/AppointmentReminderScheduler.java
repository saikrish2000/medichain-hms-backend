package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepo;
    private final NotificationService   notificationService;

    /** Every day at 8:00 AM IST */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Kolkata")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Appointment reminders for: {}", tomorrow);
        List<Appointment> list = appointmentRepo.findByAppointmentDateAndStatus(tomorrow, "CONFIRMED");
        int sent = 0;
        for (Appointment a : list) {
            try {
                if (a.getPatient() == null || a.getPatient().getUser() == null) continue;
                var u = a.getPatient().getUser();
                String name   = u.getFirstName() + " " + u.getLastName();
                String doc    = a.getDoctor() != null ? a.getDoctor().getUser().getFirstName()+" "+a.getDoctor().getUser().getLastName() : "Doctor";
                String dtStr  = a.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd MMM"))+" at "+a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
                notificationService.sendAppointmentReminder(u.getEmail(), name, doc, a.getAppointmentNumber(), a.getAppointmentDate(), a.getAppointmentTime());
                if (u.getPhone() != null && !u.getPhone().isBlank())
                    notificationService.sendAppointmentReminderSms(u.getPhone(), name, doc, dtStr);
                sent++;
            } catch (Exception e) { log.error("Reminder failed for appt {}: {}", a.getId(), e.getMessage()); }
        }
        log.info("Reminders sent: {}/{}", sent, list.size());
    }
}
