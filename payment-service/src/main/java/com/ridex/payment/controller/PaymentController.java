package com.ridex.payment.controller;

import com.ridex.payment.entity.Payment;
import com.ridex.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment Service", description = "Simulated payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    @Operation(summary = "Process payment — supports WALLET, UPI, CARD")
    public ResponseEntity<Payment> pay(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.processPayment(payment));
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "Get payment history for a user")
    public ResponseEntity<List<Payment>> history(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getHistory(userId));
    }
}
