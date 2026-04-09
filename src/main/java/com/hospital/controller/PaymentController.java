package com.hospital.controller;

import com.hospital.service.RazorpayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Razorpay payment gateway")
public class PaymentController {

    private final RazorpayService razorpayService;

    /** Step 1 — Create Razorpay order for an invoice */
    @PostMapping("/create-order/{invoiceId}")
    public ResponseEntity<?> createOrder(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(razorpayService.createOrder(invoiceId));
    }

    /** Step 2 — Verify payment after Razorpay callback */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> body) {
        String orderId    = body.get("razorpay_order_id");
        String paymentId  = body.get("razorpay_payment_id");
        String signature  = body.get("razorpay_signature");
        return ResponseEntity.ok(razorpayService.verifyAndCapture(orderId, paymentId, signature));
    }
}
