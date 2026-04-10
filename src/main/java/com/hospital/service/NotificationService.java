package com.hospital.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TwilioService  twilioService;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:noreply@medichain.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private boolean isMailConfigured() {
        return mailUsername != null && !mailUsername.isBlank();
    }

    // ── AUTH ────────────────────────────────────────────────────
    @Async
    public void sendEmailVerification(String toEmail, String fullName, String token) {
        String url = baseUrl + "/verify-email?token=" + token;
        sendHtmlEmail(toEmail, "Verify your MediChain account",
            buildTemplate("Verify Your Email 📧", "Hi " + fullName + ",",
                "Thanks for registering. Click below to verify your email address.", url, "Verify Email"));
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String url = baseUrl + "/reset-password?token=" + token;
        sendHtmlEmail(toEmail, "Reset your MediChain password",
            buildTemplate("Reset Your Password 🔒", "Hi " + fullName + ",",
                "Click below to set a new password. This link expires in 2 hours.", url, "Reset Password"));
    }

    @Async
    public void sendApprovalNotification(String toEmail, String fullName, String role) {
        sendHtmlEmail(toEmail, "Your MediChain account has been approved!",
            buildTemplate("Account Approved 🎉", "Hi " + fullName + ",",
                "Your <strong>" + role + "</strong> account has been approved. You can now login.",
                baseUrl + "/login", "Login Now"));
    }

    @Async
    public void sendRejectionNotification(String toEmail, String fullName, String reason) {
        sendHtmlEmail(toEmail, "MediChain Account Application Update",
            buildTemplate("Application Update", "Hi " + fullName + ",",
                "Your application could not be approved at this time.<br><br><strong>Reason:</strong> " + reason,
                null, null));
    }

    // ── APPOINTMENTS ────────────────────────────────────────────
    @Async
    public void sendAppointmentRequestToDoctor(String doctorEmail, String doctorName,
                                                String patientName, LocalDate date, LocalTime time) {
        String formatted = fmt(date, time);
        sendHtmlEmail(doctorEmail, "New Appointment Request — " + patientName,
            buildTemplate("New Appointment Request 📋", "Hi Dr. " + doctorName + ",",
                "<strong>" + patientName + "</strong> has requested an appointment on <strong>" + formatted + "</strong>.",
                baseUrl + "/doctor/appointments", "Review Request"));
    }

    @Async
    public void sendAppointmentConfirmationToPatient(String patientEmail, String patientName,
                                                      String appointmentNumber, String doctorName,
                                                      LocalDate date, LocalTime time) {
        String formatted = fmt(date, time);
        sendHtmlEmail(patientEmail, "Appointment Confirmed — #" + appointmentNumber,
            buildTemplate("Appointment Confirmed ✅", "Hi " + patientName + ",",
                "Your appointment with <strong>Dr. " + doctorName + "</strong> on <strong>" + formatted
                + "</strong> is confirmed.<br>Ref: <strong>#" + appointmentNumber + "</strong>",
                baseUrl + "/patient/appointments", "View Appointment"));
    }

    @Async
    public void sendAppointmentStatusUpdate(String patientEmail, String patientName,
                                             String appointmentNumber, String status,
                                             LocalDate date, LocalTime time) {
        String formatted = fmt(date, time);
        String emoji   = "CONFIRMED".equals(status) ? "✅" : ("REJECTED".equals(status) ? "❌" : "🚫");
        String heading = "Appointment " + capitalize(status) + " " + emoji;
        String msg     = "CONFIRMED".equals(status)
            ? "Your appointment (Ref: <strong>#" + appointmentNumber + "</strong>) on <strong>" + formatted + "</strong> is confirmed."
            : "Your appointment (Ref: <strong>#" + appointmentNumber + "</strong>) has been " + status.toLowerCase() + ".";
        sendHtmlEmail(patientEmail, "Appointment " + capitalize(status),
            buildTemplate(heading, "Hi " + patientName + ",", msg,
                baseUrl + "/patient/appointments", "View Appointments"));
    }

    @Async
    public void sendAppointmentReminder(String toEmail, String fullName, String doctorName,
                                         String appointmentNumber, LocalDate date, LocalTime time) {
        String formatted = fmt(date, time);
        sendHtmlEmail(toEmail, "Appointment Reminder — Tomorrow",
            buildTemplate("Appointment Reminder ⏰", "Hi " + fullName + ",",
                "Reminder: appointment with <strong>Dr. " + doctorName + "</strong> tomorrow, <strong>" + formatted
                + "</strong>.<br>Ref: <strong>#" + appointmentNumber + "</strong>",
                baseUrl + "/patient/appointments", "View Appointment"));
    }

    @Async
    public void sendPaymentConfirmation(String toEmail, String fullName,
                                         String invoiceNumber, String amount) {
        sendHtmlEmail(toEmail, "Payment Received — #" + invoiceNumber,
            buildTemplate("Payment Confirmed 💳", "Hi " + fullName + ",",
                "Payment of <strong>₹" + amount + "</strong> received for Invoice <strong>#" + invoiceNumber + "</strong>.",
                baseUrl + "/patient/bills", "View Invoice"));
    }

    // ── SMS ─────────────────────────────────────────────────────
    @Async
    public void sendSms(String phone, String message) {
        twilioService.sendSms(phone, message);
    }

    @Async
    public void sendAppointmentConfirmationSms(String phone, String patientName,
                                                String doctorName, String dateTime, String ref) {
        twilioService.sendAppointmentConfirmationSms(phone, patientName, doctorName, dateTime, ref);
    }

    @Async
    public void sendAppointmentReminderSms(String phone, String patientName,
                                            String doctorName, String dateTime) {
        twilioService.sendAppointmentReminderSms(phone, patientName, doctorName, dateTime);
    }

    @Async
    public void sendPaymentReceiptSms(String phone, String patientName,
                                       String invoiceNumber, String amount) {
        twilioService.sendPaymentReceiptSms(phone, patientName, invoiceNumber, amount);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────
    private String fmt(LocalDate date, LocalTime time) {
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
             + " at " + time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        if (!isMailConfigured()) {
            log.info("[EMAIL STUB] To: {} | Subject: {} | (configure MAIL_USERNAME to send real emails)", to, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent → {}", to);
        } catch (Exception e) {
            log.warn("Email failed → {}: {} (check MAIL_USERNAME/MAIL_PASSWORD)", to, e.getMessage());
        }
    }

    private String buildTemplate(String heading, String greeting, String body,
                                  String btnUrl, String btnText) {
        String btn = (btnUrl != null)
            ? "<div style='text-align:center;margin-top:24px;'><a href='" + btnUrl
              + "' style='display:inline-block;padding:12px 28px;background:#6C63FF;color:#fff;"
              + "border-radius:50px;text-decoration:none;font-weight:700;font-size:15px;'>"
              + btnText + "</a></div>" : "";
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#F0F4FF;font-family:Arial,sans-serif;'>"
            + "<div style='max-width:560px;margin:40px auto;background:#fff;border-radius:20px;"
            + "border:2px solid #E2E8FF;box-shadow:6px 6px 0 #D0CEFF;overflow:hidden;'>"
            + "<div style='background:linear-gradient(135deg,#6C63FF,#FF6584);padding:28px;text-align:center;'>"
            + "<div style='font-size:24px;font-weight:800;color:#fff;'>🏥 MediChain HMS</div></div>"
            + "<div style='padding:28px;'><h2 style='color:#1a1a2e;margin-bottom:8px;'>" + heading + "</h2>"
            + "<p style='color:#444;'>" + greeting + "</p>"
            + "<div style='color:#333;line-height:1.7;'>" + body + "</div>" + btn + "</div>"
            + "<div style='background:#F8F9FF;padding:16px;text-align:center;color:#888;font-size:12px;'>"
            + "MediChain HMS · <a href='mailto:support@medichain.com' style='color:#6C63FF;'>support@medichain.com</a>"
            + "</div></div></body></html>";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
