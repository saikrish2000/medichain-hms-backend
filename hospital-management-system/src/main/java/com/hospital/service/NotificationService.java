package com.hospital.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${server.port:8080}")
    private String serverPort;

    // ── EMAIL ──────────────────────────────────────────────

    @Async
    public void sendEmailVerification(String toEmail, String fullName, String token) {
        String subject = "Verify your HMS account";
        String verifyUrl = "http://localhost:" + serverPort + "/verify-email?token=" + token;
        String html = buildEmailTemplate(
            "Verify Your Email 📧",
            "Hi " + fullName + ",",
            "Thanks for registering on the Hospital Management System. " +
            "Please click the button below to verify your email address.",
            verifyUrl,
            "Verify Email"
        );
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String subject = "Reset your HMS password";
        String resetUrl = "http://localhost:" + serverPort + "/reset-password?token=" + token;
        String html = buildEmailTemplate(
            "Reset Your Password 🔒",
            "Hi " + fullName + ",",
            "We received a request to reset your password. " +
            "Click the button below to set a new password. This link expires in 2 hours.",
            resetUrl,
            "Reset Password"
        );
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async
    public void sendAppointmentConfirmation(String toEmail, String fullName,
                                             String doctorName, String date, String time) {
        String subject = "Appointment Confirmed — HMS";
        String html = buildEmailTemplate(
            "Appointment Confirmed ✅",
            "Hi " + fullName + ",",
            "Your appointment with Dr. " + doctorName +
            " has been confirmed for <strong>" + date + "</strong> at <strong>" + time + "</strong>. " +
            "Please arrive 10 minutes before your scheduled time.",
            null, null
        );
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async
    public void sendApprovalNotification(String toEmail, String fullName, String role) {
        String subject = "Your HMS account has been approved!";
        String html = buildEmailTemplate(
            "Account Approved 🎉",
            "Hi " + fullName + ",",
            "Congratulations! Your " + role + " account on the Hospital Management System " +
            "has been approved by the administrator. You can now login and access your dashboard.",
            "http://localhost:" + serverPort + "/login",
            "Login Now"
        );
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async
    public void sendRejectionNotification(String toEmail, String fullName, String reason) {
        String subject = "HMS Account Application Update";
        String html = buildEmailTemplate(
            "Application Update",
            "Hi " + fullName + ",",
            "We regret to inform you that your account application could not be approved at this time. " +
            "<br><br><strong>Reason:</strong> " + reason +
            "<br><br>If you believe this is an error, please contact our support team.",
            null, null
        );
        sendHtmlEmail(toEmail, subject, html);
    }

    // ── SMS (Twilio) ───────────────────────────────────────
    // Twilio integration will be added in Step 14 (Notifications module)
    // Stub method to prevent compilation errors
    @Async
    public void sendSms(String phoneNumber, String message) {
        log.info("[SMS STUB] To: {} | Message: {}", phoneNumber, message);
        // Actual Twilio implementation added in Step 14
    }

    // ── PRIVATE HELPERS ────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildEmailTemplate(String heading, String greeting, String body,
                                       String buttonUrl, String buttonText) {
        String button = (buttonUrl != null) ?
            "<a href='" + buttonUrl + "' style='display:inline-block;padding:12px 28px;" +
            "background:#6C63FF;color:#fff;border-radius:50px;text-decoration:none;" +
            "font-weight:700;font-size:15px;margin-top:20px;'>" + buttonText + "</a>" : "";

        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#F0F4FF;font-family:'Nunito',sans-serif;">
              <div style="max-width:560px;margin:40px auto;background:#fff;border-radius:24px;
                          border:2.5px solid #E2E8FF;box-shadow:6px 6px 0px #D0CEFF;overflow:hidden;">
                <div style="background:linear-gradient(135deg,#6C63FF,#FF6584);padding:32px;text-align:center;">
                  <div style="font-size:36px;">🏥</div>
                  <div style="color:#fff;font-size:22px;font-weight:800;margin-top:8px;">%s</div>
                  <div style="color:rgba(255,255,255,0.8);font-size:13px;margin-top:4px;">
                    Hospital Management System
                  </div>
                </div>
                <div style="padding:32px;">
                  <p style="font-size:16px;font-weight:700;color:#2D3250;">%s</p>
                  <p style="font-size:14px;color:#7B7F9E;line-height:1.7;margin-top:12px;">%s</p>
                  <div style="text-align:center;">%s</div>
                </div>
                <div style="background:#F0F4FF;padding:20px;text-align:center;
                            font-size:12px;color:#7B7F9E;">
                  This email was sent by Hospital Management System. Please do not reply.
                </div>
              </div>
            </body>
            </html>
            """.formatted(heading, greeting, body, button);
    }
}
