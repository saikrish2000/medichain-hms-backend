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

    @Value("${twilio.account.sid:}") private String accountSid;
    @Value("${twilio.auth.token:}")  private String authToken;
    @Value("${twilio.phone.number:}") private String fromNumber;

    private boolean enabled = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            enabled = true;
            log.info("Twilio SMS initialized ✅");
        } else {
            log.info("Twilio not configured — SMS will be logged only (set TWILIO_ACCOUNT_SID etc.)");
        }
    }

    @Async
    public void sendSms(String toPhone, String messageBody) {
        if (!enabled) { log.info("[SMS STUB] To: {} | {}", toPhone, messageBody); return; }
        try {
            String to = toPhone.startsWith("+") ? toPhone : "+91" + toPhone;
            Message msg = Message.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), messageBody).create();
            log.info("SMS sent → {} | SID: {}", to, msg.getSid());
        } catch (Exception e) {
            log.warn("SMS failed → {}: {}", toPhone, e.getMessage());
        }
    }

    public void sendAppointmentConfirmationSms(String phone, String patient, String doctor, String dateTime, String ref) {
        sendSms(phone, String.format("MediChain: Hi %s, appointment with Dr.%s on %s confirmed. Ref:#%s", patient, doctor, dateTime, ref));
    }
    public void sendAppointmentReminderSms(String phone, String patient, String doctor, String dateTime) {
        sendSms(phone, String.format("MediChain: Reminder %s - appointment with Dr.%s tomorrow at %s", patient, doctor, dateTime));
    }
    public void sendPaymentReceiptSms(String phone, String patient, String invoiceNo, String amount) {
        sendSms(phone, String.format("MediChain: Hi %s, payment Rs.%s received for Invoice #%s. Thank you!", patient, amount, invoiceNo));
    }
    public void sendEmergencyAlertSms(String phone, String message) {
        sendSms(phone, "MediChain EMERGENCY: " + message);
    }
}
