package com.hospital.controller;

import com.hospital.service.RazorpayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Razorpay payment gateway")
public class PaymentController {

    private final RazorpayService razorpayService;

    @PostMapping("/create-order/{invoiceId}")
    public ResponseEntity<?> createOrder(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(razorpayService.createOrder(invoiceId));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String,String> body) {
        return ResponseEntity.ok(razorpayService.verifyAndCapture(
            body.get("razorpay_order_id"),
            body.get("razorpay_payment_id"),
            body.get("razorpay_signature")));
    }
}
