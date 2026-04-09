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
    private final TwilioService twilioService;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ── AUTH ───────────────────────────────────────────────

    @Async
    public void sendEmailVerification(String toEmail, String fullName, String token) {
        String url = baseUrl + "/verify-email?token=" + token;
        sendHtmlEmail(toEmail, "Verify your HMS account",
            buildTemplate("Verify Your Email 📧", "Hi " + fullName + ",",
                "Thanks for registering on MediChain HMS. Click the button below to verify your email address.",
                url, "Verify Email"));
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String url = baseUrl + "/reset-password?token=" + token;
        sendHtmlEmail(toEmail, "Reset your HMS password",
            buildTemplate("Reset Your Password 🔒", "Hi " + fullName + ",",
                "We received a request to reset your password. Click below to set a new password. This link expires in 2 hours.",
                url, "Reset Password"));
    }

    @Async
    public void sendApprovalNotification(String toEmail, String fullName, String role) {
        sendHtmlEmail(toEmail, "Your HMS account has been approved!",
            buildTemplate("Account Approved 🎉", "Hi " + fullName + ",",
                "Congratulations! Your <strong>" + role + "</strong> account on MediChain HMS " +
                "has been approved by the administrator. You can now login and access your dashboard.",
                baseUrl + "/login", "Login Now"));
    }

    @Async
    public void sendRejectionNotification(String toEmail, String fullName, String reason) {
        sendHtmlEmail(toEmail, "HMS Account Application Update",
            buildTemplate("Application Update", "Hi " + fullName + ",",
                "We regret to inform you that your account application could not be approved at this time." +
                "<br><br><strong>Reason:</strong> " + reason +
                "<br><br>If you believe this is an error, please contact our support team.", null, null));
    }

    // ── APPOINTMENTS ───────────────────────────────────────

    @Async
    public void sendAppointmentRequestToDoctor(String doctorEmail, String doctorName,
                                                String patientName, LocalDate date, LocalTime time) {
        String formatted = formatDateTime(date, time);
        sendHtmlEmail(doctorEmail, "New Appointment Request — " + patientName,
            buildTemplate("New Appointment Request 📋", "Hi Dr. " + doctorName + ",",
                "<strong>" + patientName + "</strong> has requested an appointment with you on " +
                "<strong>" + formatted + "</strong>.<br><br>Please login to confirm or reject the request.",
                baseUrl + "/doctor/appointments?filter=pending", "Review Request"));
    }

    @Async
    public void sendAppointmentConfirmationToPatient(String patientEmail, String patientName,
                                                      String appointmentNumber, String doctorName,
                                                      LocalDate date, LocalTime time) {
        String formatted = formatDateTime(date, time);
        sendHtmlEmail(patientEmail, "Appointment Confirmed — " + appointmentNumber,
            buildTemplate("Appointment Confirmed ✅", "Hi " + patientName + ",",
                "Your appointment with <strong>Dr. " + doctorName + "</strong> on <strong>" + formatted + "</strong> " +
                "is confirmed.<br><br>Reference: <strong>#" + appointmentNumber + "</strong><br><br>" +
                "Please arrive 10 minutes before your scheduled time.",
                baseUrl + "/patient/appointments", "View Appointment"));
    }

    @Async
    public void sendAppointmentStatusUpdate(String patientEmail, String patientName,
                                             String appointmentNumber, String status,
                                             LocalDate date, LocalTime time) {
        String formatted = formatDateTime(date, time);
        String emoji = switch (status) {
            case "CONFIRMED" -> "✅";
            case "REJECTED"  -> "❌";
            default          -> "🚫";
        };
        String heading = "Appointment " + capitalize(status) + " " + emoji;
        String msg = switch (status) {
            case "CONFIRMED" -> "Your appointment (Ref: <strong>#" + appointmentNumber + "</strong>) on " +
                                "<strong>" + formatted + "</strong> has been <strong>confirmed</strong>. Please arrive 10 minutes early.";
            case "REJECTED"  -> "Your appointment request (Ref: <strong>#" + appointmentNumber + "</strong>) " +
                                "for <strong>" + formatted + "</strong> has been rejected. You may book with another doctor.";
            default          -> "Your appointment (Ref: <strong>#" + appointmentNumber + "</strong>) has been cancelled.";
        };
        sendHtmlEmail(patientEmail, "Appointment " + capitalize(status) + " — MediChain HMS",
            buildTemplate(heading, "Hi " + patientName + ",", msg,
                baseUrl + "/patient/appointments", "View My Appointments"));
    }

    @Async
    public void sendAppointmentReminder(String toEmail, String fullName, String doctorName,
                                         String appointmentNumber, LocalDate date, LocalTime time) {
        String formatted = formatDateTime(date, time);
        sendHtmlEmail(toEmail, "Appointment Reminder — Tomorrow",
            buildTemplate("Appointment Reminder ⏰", "Hi " + fullName + ",",
                "Reminder: appointment with <strong>Dr. " + doctorName + "</strong> tomorrow, " +
                "<strong>" + formatted + "</strong>.<br><br>Ref: <strong>#" + appointmentNumber + "</strong>",
                baseUrl + "/patient/appointments", "View Appointment"));
    }

    @Async
    public void sendPaymentConfirmation(String toEmail, String fullName,
                                         String invoiceNumber, String amount) {
        sendHtmlEmail(toEmail, "Payment Received — " + invoiceNumber,
            buildTemplate("Payment Confirmed 💳", "Hi " + fullName + ",",
                "We've received your payment of <strong>₹" + amount + "</strong> for Invoice " +
                "<strong>#" + invoiceNumber + "</strong>.<br><br>Thank you for choosing MediChain HMS.",
                baseUrl + "/patient/bills", "View Invoice"));
    }

    // ── SMS DELEGATES ───────────────────────────────────────

    @Async
    public void sendSms(String phoneNumber, String message) {
        twilioService.sendSms(phoneNumber, message);
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

    // ── PRIVATE HELPERS ────────────────────────────────────

    private String formatDateTime(LocalDate date, LocalTime time) {
        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
             + " at " + time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private void sendHtmlEmail(String to, String subject, String html) {
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
            log.error("Email failed → {}: {}", to, e.getMessage());
        }
    }

    private String buildTemplate(String heading, String greeting, String body,
                                  String buttonUrl, String buttonText) {
        String button = (buttonUrl != null)
            ? "<div style='text-align:center;margin-top:24px;'>" +
              "<a href='" + buttonUrl + "' style='display:inline-block;padding:12px 28px;" +
              "background:#6C63FF;color:#fff;border-radius:50px;text-decoration:none;" +
              "font-weight:700;font-size:15px;'>" + buttonText + "</a></div>"
            : "";
        return """
            <!DOCTYPE html><html><body style="margin:0;padding:0;background:#F0F4FF;font-family:Arial,sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#fff;border-radius:24px;
                          border:2.5px solid #E2E8FF;box-shadow:6px 6px 0px #D0CEFF;overflow:hidden;">
                <div style="background:linear-gradient(135deg,#6C63FF,#FF6584);padding:32px;text-align:center;">
                  <div style="font-size:28px;font-weight:800;color:#fff;letter-spacing:-0.5px;">🏥 MediChain HMS</div>
                </div>
                <div style="padding:32px;">
                  <h2 style="color:#1a1a2e;font-size:22px;margin-bottom:8px;">""" + heading + """
                  </h2>
                  <p style="color:#444;font-size:15px;margin-bottom:16px;">""" + greeting + """
                  </p>
                  <div style="color:#333;font-size:15px;line-height:1.7;">""" + body + """
                  </div>""" + button + """
                </div>
                <div style="background:#F8F9FF;padding:20px;text-align:center;color:#888;font-size:12px;">
                  MediChain HMS · Do not reply to this email · 
                  <a href="mailto:support@medichain.com" style="color:#6C63FF;">support@medichain.com</a>
                </div>
              </div>
            </body></html>
            """;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
