package com.hospital.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioService {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String fromNumber;

    private boolean enabled = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            enabled = true;
            log.info("Twilio SMS service initialized ✅");
        } else {
            log.warn("Twilio credentials not configured — SMS will be logged only");
        }
    }

    @Async
    public void sendSms(String toPhone, String messageBody) {
        if (!enabled) {
            log.info("[SMS STUB] To: {} | {}", toPhone, messageBody);
            return;
        }
        try {
            // Ensure E.164 format
            String to = toPhone.startsWith("+") ? toPhone : "+91" + toPhone;
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();
            log.info("SMS sent → {} | SID: {}", to, message.getSid());
        } catch (Exception e) {
            log.error("SMS failed → {}: {}", toPhone, e.getMessage());
        }
    }

    // ── Ready-made SMS templates ──────────────────────────

    public void sendAppointmentConfirmationSms(String phone, String patientName,
                                                String doctorName, String dateTime,
                                                String appointmentNumber) {
        String msg = String.format(
                "MediChain HMS: Hi %s, your appointment with Dr. %s on %s is confirmed. Ref: #%s. Please arrive 10 mins early.",
                patientName, doctorName, dateTime, appointmentNumber);
        sendSms(phone, msg);
    }

    public void sendAppointmentReminderSms(String phone, String patientName,
                                            String doctorName, String dateTime) {
        String msg = String.format(
                "MediChain HMS: Reminder! %s, you have an appointment with Dr. %s tomorrow at %s. Please don't miss it.",
                patientName, doctorName, dateTime);
        sendSms(phone, msg);
    }

    public void sendOtpSms(String phone, String otp) {
        String msg = String.format("MediChain HMS: Your OTP is %s. Valid for 10 minutes. Do not share.", otp);
        sendSms(phone, msg);
    }

    public void sendPaymentReceiptSms(String phone, String patientName,
                                       String invoiceNumber, String amount) {
        String msg = String.format(
                "MediChain HMS: Hi %s, payment of Rs.%s received for Invoice #%s. Thank you!",
                patientName, amount, invoiceNumber);
        sendSms(phone, msg);
    }

    public void sendEmergencyAlertSms(String phone, String message) {
        sendSms(phone, "MediChain EMERGENCY: " + message);
    }
}
