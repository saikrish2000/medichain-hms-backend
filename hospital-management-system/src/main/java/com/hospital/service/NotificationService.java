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
                "Thanks for registering on the Hospital Management System. " +
                "Please click the button below to verify your email address.",
                url, "Verify Email"));
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String url = baseUrl + "/reset-password?token=" + token;
        sendHtmlEmail(toEmail, "Reset your HMS password",
            buildTemplate("Reset Your Password 🔒", "Hi " + fullName + ",",
                "We received a request to reset your password. Click below to set a new password. " +
                "This link expires in 2 hours.",
                url, "Reset Password"));
    }

    @Async
    public void sendApprovalNotification(String toEmail, String fullName, String role) {
        sendHtmlEmail(toEmail, "Your HMS account has been approved!",
            buildTemplate("Account Approved 🎉", "Hi " + fullName + ",",
                "Congratulations! Your <strong>" + role + "</strong> account on the Hospital Management System " +
                "has been approved by the administrator. You can now login and access your dashboard.",
                baseUrl + "/login", "Login Now"));
    }

    @Async
    public void sendRejectionNotification(String toEmail, String fullName, String reason) {
        sendHtmlEmail(toEmail, "HMS Account Application Update",
            buildTemplate("Application Update", "Hi " + fullName + ",",
                "We regret to inform you that your account application could not be approved at this time." +
                "<br><br><strong>Reason:</strong> " + reason +
                "<br><br>If you believe this is an error, please contact our support team.",
                null, null));
    }

    // ── APPOINTMENTS ───────────────────────────────────────

    /** Notify doctor of a new booking request */
    @Async
    public void sendAppointmentRequestToDoctor(String doctorEmail, String doctorName,
                                                String patientName,
                                                LocalDate date, LocalTime time) {
        String formatted = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                         + " at " + time.format(DateTimeFormatter.ofPattern("hh:mm a"));
        sendHtmlEmail(doctorEmail, "New Appointment Request — " + patientName,
            buildTemplate("New Appointment Request 📋",
                "Hi Dr. " + doctorName + ",",
                "<strong>" + patientName + "</strong> has requested an appointment with you on " +
                "<strong>" + formatted + "</strong>.<br><br>" +
                "Please login to your dashboard to review and confirm or reject the request.",
                baseUrl + "/doctor/appointments?filter=pending",
                "Review Request"));
    }

    /** Confirm booking to patient (pending doctor approval) */
    @Async
    public void sendAppointmentConfirmationToPatient(String patientEmail, String patientName,
                                                      String appointmentNumber,
                                                      String doctorName,
                                                      LocalDate date, LocalTime time) {
        String formatted = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                         + " at " + time.format(DateTimeFormatter.ofPattern("hh:mm a"));
        sendHtmlEmail(patientEmail, "Appointment Request Received — " + appointmentNumber,
            buildTemplate("Appointment Request Received ⏳",
                "Hi " + patientName + ",",
                "Your appointment request with <strong>Dr. " + doctorName + "</strong> " +
                "has been received for <strong>" + formatted + "</strong>.<br><br>" +
                "Reference: <strong>#" + appointmentNumber + "</strong><br><br>" +
                "You will receive another notification once the doctor confirms your appointment. " +
                "Please arrive 10 minutes before your scheduled time.",
                baseUrl + "/appointments/my",
                "View My Appointments"));
    }

    /** Status update to patient (CONFIRMED / REJECTED / CANCELLED) */
    @Async
    public void sendAppointmentStatusUpdate(String patientEmail, String patientName,
                                             String appointmentNumber, String status,
                                             LocalDate date, LocalTime time) {
        String formatted = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                         + " at " + time.format(DateTimeFormatter.ofPattern("hh:mm a"));

        String emoji   = status.equals("CONFIRMED") ? "✅" : (status.equals("REJECTED") ? "❌" : "🚫");
        String heading = "Appointment " + capitalize(status) + " " + emoji;
        String msg;
        if ("CONFIRMED".equals(status))
            msg = "Your appointment (Reference: <strong>#" + appointmentNumber + "</strong>) " +
                  "scheduled for <strong>" + formatted + "</strong> has been <strong>confirmed</strong> " +
                  "by your doctor. Please arrive 10 minutes early.";
        else if ("REJECTED".equals(status))
            msg = "Your appointment request (Reference: <strong>#" + appointmentNumber + "</strong>) " +
                  "for <strong>" + formatted + "</strong> has been rejected by the doctor. " +
                  "You can book a new appointment with another available doctor.";
        else
            msg = "Your appointment (Reference: <strong>#" + appointmentNumber + "</strong>) " +
                  "has been cancelled.";

        sendHtmlEmail(patientEmail, "Appointment " + capitalize(status) + " — HMS",
            buildTemplate(heading, "Hi " + patientName + ",", msg,
                baseUrl + "/appointments/my", "View My Appointments"));
    }

    /** Appointment reminder (called 24h before) */
    @Async
    public void sendAppointmentReminder(String toEmail, String fullName,
                                         String doctorName, String appointmentNumber,
                                         LocalDate date, LocalTime time) {
        String formatted = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                         + " at " + time.format(DateTimeFormatter.ofPattern("hh:mm a"));
        sendHtmlEmail(toEmail, "Appointment Reminder — Tomorrow",
            buildTemplate("Appointment Reminder ⏰", "Hi " + fullName + ",",
                "This is a reminder that you have an appointment with <strong>Dr. " + doctorName + "</strong> " +
                "tomorrow, <strong>" + formatted + "</strong>.<br><br>" +
                "Reference: <strong>#" + appointmentNumber + "</strong><br><br>" +
                "Please arrive at least 10 minutes early and bring any previous medical records.",
                baseUrl + "/appointments/my", "View Appointment"));
    }

    // ── SMS (Twilio — implemented in Step 14) ─────────────
    @Async
    public void sendSms(String phoneNumber, String message) {
        log.info("[SMS STUB] To: {} | Message: {}", phoneNumber, message);
    }

    // ── PRIVATE HELPERS ────────────────────────────────────

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
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#F0F4FF;font-family:Arial,sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#fff;border-radius:24px;
                          border:2.5px solid #E2E8FF;box-shadow:6px 6px 0px #D0CEFF;overflow:hidden;">
                <div style="background:linear-gradient(135deg,#6C63FF,#FF6584);padding:32px;text-align:center;">
                  <div style="font-size:36px;">🏥</div>
                  <div style="color:#fff;font-size:22px;font-weight:800;margin-top:8px;">%s</div>
                  <div style="color:rgba(255,255,255,0.8);font-size:13px;margin-top:4px;">Hospital Management System</div>
                </div>
                <div style="padding:32px;">
                  <p style="font-size:16px;font-weight:700;color:#2D3250;margin:0;">%s</p>
                  <p style="font-size:14px;color:#7B7F9E;line-height:1.8;margin-top:12px;">%s</p>
                  %s
                </div>
                <div style="background:#F0F4FF;padding:20px;text-align:center;font-size:12px;color:#7B7F9E;">
                  Hospital Management System · Do not reply to this email.
                </div>
              </div>
            </body>
            </html>
            """.formatted(heading, greeting, body, button);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
