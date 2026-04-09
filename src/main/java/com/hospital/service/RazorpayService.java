package com.hospital.service;

import com.hospital.entity.Invoice;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.InvoiceRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final InvoiceRepository invoiceRepo;

    /** Create a Razorpay order for an invoice */
    public Map<String, Object> createOrder(Long invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        if ("PAID".equals(invoice.getStatus()))
            throw new BadRequestException("Invoice is already paid");

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            // Razorpay expects amount in paise (INR * 100)
            long amountPaise = invoice.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "INV-" + invoiceId);
            orderRequest.put("payment_capture", 1);

            Order order = client.orders.create(orderRequest);

            // Save razorpay order id on invoice
            invoice.setTransactionId(order.get("id").toString());
            invoiceRepo.save(invoice);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("razorpayOrderId", order.get("id").toString());
            response.put("amount", amountPaise);
            response.put("currency", "INR");
            response.put("keyId", keyId);
            response.put("invoiceId", invoiceId);
            response.put("invoiceNumber", invoice.getInvoiceNumber());
            return response;

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new BadRequestException("Payment initiation failed: " + e.getMessage());
        }
    }

    /** Verify Razorpay webhook signature and mark invoice paid */
    @Transactional
    public Map<String, String> verifyAndCapture(String razorpayOrderId,
                                                  String razorpayPaymentId,
                                                  String razorpaySignature) {
        // Verify signature: HMAC-SHA256(orderId + "|" + paymentId, keySecret)
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String computed = hmacSha256(payload, keySecret);

        if (!computed.equalsIgnoreCase(razorpaySignature))
            throw new BadRequestException("Payment verification failed — invalid signature");

        // Find invoice by razorpay order id (stored in transaction_id)
        Invoice invoice = invoiceRepo.findByTransactionId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "razorpayOrderId", razorpayOrderId));

        invoice.setStatus("PAID");
        invoice.setPaymentMethod("RAZORPAY");
        invoice.setTransactionId(razorpayPaymentId);
        invoice.setAmountPaid(invoice.getTotalAmount());
        invoice.setPaidAt(java.time.LocalDateTime.now());
        invoiceRepo.save(invoice);

        log.info("Payment verified and captured for invoice: {}", invoice.getInvoiceNumber());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("invoiceNumber", invoice.getInvoiceNumber());
        result.put("paymentId", razorpayPaymentId);
        return result;
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }
}
